<template>
  <div class="app-container">
    <header class="app-header">
      <div class="header-left">
        <div class="logo">
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="3" y="3" width="18" height="18" rx="2" />
            <path d="M7 12h10M12 7v10" />
          </svg>
        </div>
        <h1 class="app-title">数字逻辑电路模拟器</h1>
      </div>
      <div class="header-right">
        <div class="toolbar-group">
          <t-button
            theme="default"
            :disabled="!canUndo"
            @click="undo"
            class="toolbar-btn"
            title="撤销 (Ctrl+Z)"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M3 7v6h6"/>
              <path d="M21 17a9 9 0 0 0-9-9 9 9 0 0 0-6.7 3L3 13"/>
            </svg>
            <span class="btn-text">撤销</span>
          </t-button>
          <t-button
            theme="default"
            :disabled="!canRedo"
            @click="redo"
            class="toolbar-btn"
            title="重做 (Ctrl+Y)"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 7v6h-6"/>
              <path d="M3 17a9 9 0 0 1 9-9 9 9 0 0 1 6.7 3L21 13"/>
            </svg>
            <span class="btn-text">重做</span>
          </t-button>
        </div>
        <t-select v-model="currentCircuitId" placeholder="选择电路" style="width: 200px;">
          <t-option v-for="c in circuits" :key="c.id" :value="c.id" :label="c.name" />
        </t-select>
        <t-button theme="primary" @click="createCircuit">新建电路</t-button>
        <t-button theme="default" @click="saveCircuit">保存</t-button>
      </div>
    </header>

    <div class="app-body">
      <GatePanel @drag-start="onDragStart" @load-template="onLoadTemplate" />
      <LogicCanvas
        ref="canvasRef"
        :gates="gates"
        :connections="connections"
        :selected-gate-ids="selectedGateIds"
        @drop-gate="onDropGate"
        @update-gate="onUpdateGate"
        @update-gates="onUpdateGates"
        @delete-gate="onDeleteGate"
        @delete-gates="onDeleteGates"
        @add-connection="onAddConnection"
        @delete-connection="onDeleteConnection"
        @toggle-input="onToggleInput"
        @select-gate="onSelectGate"
        @select-gates="onSelectGates"
        @canvas-click="onCanvasClick"
        @move-gates-end="onMoveGatesEnd"
      />
      <PropertyPanel
        :gate="selectedGate"
        :gates="gates"
        :connections="connections"
        @update-label="onUpdateGateLabel"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import GatePanel from './components/GatePanel.vue'
import LogicCanvas from './components/LogicCanvas.vue'
import PropertyPanel from './components/PropertyPanel.vue'
import { circuitApi } from './api'

const circuits = ref([])
const currentCircuitId = ref(null)
const gates = ref([])
const connections = ref([])
const canvasRef = ref(null)
const selectedGateIds = ref([])
const selectedGate = computed(() => {
  if (selectedGateIds.value.length !== 1) return null
  const g = gates.value.find(g => g.id === selectedGateIds.value[0])
  return g ? { ...g } : null
})
let gateIdCounter = 0
let connectionIdCounter = 0

const undoStack = ref([])
const redoStack = ref([])

const canUndo = computed(() => undoStack.value.length > 0)
const canRedo = computed(() => redoStack.value.length > 0)

const deepClone = (obj) => JSON.parse(JSON.stringify(obj))

const pushHistory = (action) => {
  undoStack.value.push(action)
  redoStack.value = []
}

const executeUndo = () => {
  if (undoStack.value.length === 0) return
  const action = undoStack.value.pop()
  redoStack.value.push(action)
  applyAction(action, true)
}

const executeRedo = () => {
  if (redoStack.value.length === 0) return
  const action = redoStack.value.pop()
  undoStack.value.push(action)
  applyAction(action, false)
}

const applyAction = (action, isUndo) => {
  const { type, before, after } = action
  const data = isUndo ? before : after

  switch (type) {
    case 'ADD_GATE': {
      if (isUndo) {
        gates.value = gates.value.filter(g => g.id !== data.gate.id)
        connections.value = connections.value.filter(
          c => c.fromGateId !== data.gate.id && c.toGateId !== data.gate.id
        )
        selectedGateIds.value = selectedGateIds.value.filter(id => id !== data.gate.id)
      } else {
        gates.value.push({ ...data.gate })
        if (data.connections) {
          data.connections.forEach(c => connections.value.push({ ...c }))
        }
      }
      break
    }
    case 'DELETE_GATE': {
      if (isUndo) {
        gates.value.push({ ...data.gate })
        if (data.connections) {
          data.connections.forEach(c => connections.value.push({ ...c }))
        }
      } else {
        gates.value = gates.value.filter(g => g.id !== data.gate.id)
        connections.value = connections.value.filter(
          c => c.fromGateId !== data.gate.id && c.toGateId !== data.gate.id
        )
        selectedGateIds.value = selectedGateIds.value.filter(id => id !== data.gate.id)
      }
      break
    }
    case 'DELETE_GATES': {
      if (isUndo) {
        data.gates.forEach(g => gates.value.push({ ...g }))
        if (data.connections) {
          data.connections.forEach(c => connections.value.push({ ...c }))
        }
      } else {
        const ids = new Set(data.gates.map(g => g.id))
        gates.value = gates.value.filter(g => !ids.has(g.id))
        connections.value = connections.value.filter(
          c => !ids.has(c.fromGateId) && !ids.has(c.toGateId)
        )
        selectedGateIds.value = selectedGateIds.value.filter(id => !ids.has(id))
      }
      break
    }
    case 'MOVE_GATE': {
      const gate = gates.value.find(g => g.id === data.gateId)
      if (gate) {
        gate.x = data.x
        gate.y = data.y
      }
      break
    }
    case 'MOVE_GATES': {
      data.gates.forEach(({ gateId, x, y }) => {
        const gate = gates.value.find(g => g.id === gateId)
        if (gate) {
          gate.x = x
          gate.y = y
        }
      })
      break
    }
    case 'ADD_CONNECTION': {
      if (isUndo) {
        connections.value = connections.value.filter(c => c.id !== data.connection.id)
      } else {
        connections.value.push({ ...data.connection })
      }
      break
    }
    case 'DELETE_CONNECTION': {
      if (isUndo) {
        connections.value.push({ ...data.connection })
      } else {
        connections.value = connections.value.filter(c => c.id !== data.connection.id)
      }
      break
    }
    case 'RENAME_GATE': {
      const gate = gates.value.find(g => g.id === data.gateId)
      if (gate) {
        gate.label = data.label
      }
      break
    }
  }

  simulate()
}

const createCircuit = async () => {
  try {
    const name = `电路 ${circuits.value.length + 1}`
    const res = await circuitApi.create(name, '')
    circuits.value.push(res.data)
    currentCircuitId.value = res.data.id
    gates.value = []
    connections.value = []
    gateIdCounter = 0
    connectionIdCounter = 0
    selectedGateIds.value = []
    undoStack.value = []
    redoStack.value = []
    MessagePlugin.success('电路创建成功')
  } catch (e) {
    MessagePlugin.error('创建失败')
  }
}

const saveCircuit = async () => {
  if (!currentCircuitId.value) {
    MessagePlugin.warning('请先选择或创建电路')
    return
  }
  try {
    await circuitApi.saveDetail(currentCircuitId.value, {
      circuitId: currentCircuitId.value,
      gates: gates.value,
      connections: connections.value
    })
    MessagePlugin.success('保存成功')
  } catch (e) {
    MessagePlugin.error('保存失败')
  }
}

const loadCircuit = async (id) => {
  if (!id) return
  try {
    const res = await circuitApi.getDetail(id)
    gates.value = res.data.gates || []
    connections.value = res.data.connections || []
    gateIdCounter = gates.value.reduce((max, g) => Math.max(max, g.id || 0), 0)
    connectionIdCounter = connections.value.reduce((max, c) => Math.max(max, c.id || 0), 0)
    selectedGateIds.value = []
    undoStack.value = []
    redoStack.value = []
  } catch (e) {
    MessagePlugin.error('加载电路失败')
  }
}

const onDragStart = (gateType) => {
}

const onDropGate = ({ type, x, y }) => {
  gateIdCounter++
  const newGate = {
    id: gateIdCounter,
    type,
    x,
    y,
    inputCount: (type === 'NOT' || type === 'INPUT' || type === 'OUTPUT') ? 1 : 2,
    outputValue: type === 'INPUT' ? false : null,
    inputValueA: null,
    inputValueB: null,
    label: ''
  }
  gates.value.push(newGate)
  pushHistory({
    type: 'ADD_GATE',
    before: { gate: { ...newGate }, connections: [] },
    after: { gate: { ...newGate }, connections: [] }
  })
  simulate()
}

let pendingMoveGate = null
const onUpdateGate = ({ id, x, y }) => {
  const gate = gates.value.find(g => g.id === id)
  if (gate) {
    if (!pendingMoveGate || pendingMoveGate.gateId !== id) {
      pendingMoveGate = {
        gateId: id,
        startX: gate.x,
        startY: gate.y
      }
    }
    gate.x = x
    gate.y = y
  }
}

const onMoveGatesEnd = ({ movedGates }) => {
  if (movedGates.length === 1) {
    const { gateId, startX, startY, endX, endY } = movedGates[0]
    if (startX !== endX || startY !== endY) {
      pushHistory({
        type: 'MOVE_GATE',
        before: { gateId, x: startX, y: startY },
        after: { gateId, x: endX, y: endY }
      })
    }
  } else if (movedGates.length > 1) {
    pushHistory({
      type: 'MOVE_GATES',
      before: {
        gates: movedGates.map(m => ({ gateId: m.gateId, x: m.startX, y: m.startY }))
      },
      after: {
        gates: movedGates.map(m => ({ gateId: m.gateId, x: m.endX, y: m.endY }))
      }
    })
  }
  pendingMoveGate = null
}

const onUpdateGates = (updates) => {
  updates.forEach(({ id, x, y }) => {
    const gate = gates.value.find(g => g.id === id)
    if (gate) {
      gate.x = x
      gate.y = y
    }
  })
}

const onDeleteGate = (id) => {
  const gate = gates.value.find(g => g.id === id)
  if (!gate) return
  const affectedConnections = connections.value.filter(
    c => c.fromGateId === id || c.toGateId === id
  )
  pushHistory({
    type: 'DELETE_GATE',
    before: {
      gate: { ...gate },
      connections: deepClone(affectedConnections)
    },
    after: {
      gate: { ...gate },
      connections: deepClone(affectedConnections)
    }
  })
  gates.value = gates.value.filter(g => g.id !== id)
  connections.value = connections.value.filter(c => c.fromGateId !== id && c.toGateId !== id)
  selectedGateIds.value = selectedGateIds.value.filter(sid => sid !== id)
  simulate()
}

const onDeleteGates = (ids) => {
  if (!ids || ids.length === 0) return
  const idSet = new Set(ids)
  const deletedGates = gates.value.filter(g => idSet.has(g.id)).map(g => ({ ...g }))
  const affectedConnections = connections.value.filter(
    c => idSet.has(c.fromGateId) || idSet.has(c.toGateId)
  )
  if (deletedGates.length === 1) {
    pushHistory({
      type: 'DELETE_GATE',
      before: {
        gate: deletedGates[0],
        connections: deepClone(affectedConnections)
      },
      after: {
        gate: deletedGates[0],
        connections: deepClone(affectedConnections)
      }
    })
  } else {
    pushHistory({
      type: 'DELETE_GATES',
      before: {
        gates: deletedGates,
        connections: deepClone(affectedConnections)
      },
      after: {
        gates: deletedGates,
        connections: deepClone(affectedConnections)
      }
    })
  }
  gates.value = gates.value.filter(g => !idSet.has(g.id))
  connections.value = connections.value.filter(
    c => !idSet.has(c.fromGateId) && !idSet.has(c.toGateId)
  )
  selectedGateIds.value = selectedGateIds.value.filter(sid => !idSet.has(sid))
  simulate()
}

const onSelectGate = (gate) => {
  selectedGateIds.value = [gate.id]
}

const onSelectGates = (ids) => {
  selectedGateIds.value = [...ids]
}

const onClosePropertyPanel = () => {
  selectedGateIds.value = []
}

const onUpdateGateLabel = (label) => {
  if (selectedGateIds.value.length !== 1) return
  const gateId = selectedGateIds.value[0]
  const gate = gates.value.find(g => g.id === gateId)
  if (gate) {
    const oldLabel = gate.label
    pushHistory({
      type: 'RENAME_GATE',
      before: { gateId, label: oldLabel },
      after: { gateId, label: label }
    })
    gate.label = label
  }
}

const onCanvasClick = () => {
  selectedGateIds.value = []
}

const onAddConnection = ({ fromGateId, fromPort, toGateId, toPort }) => {
  const exists = connections.value.some(
    c => c.toGateId === toGateId && c.toPort === toPort
  )
  if (exists) {
    return
  }
  connectionIdCounter++
  const newConn = {
    id: connectionIdCounter,
    fromGateId,
    fromPort,
    toGateId,
    toPort
  }
  connections.value.push(newConn)
  pushHistory({
    type: 'ADD_CONNECTION',
    before: { connection: { ...newConn } },
    after: { connection: { ...newConn } }
  })
  simulate()
}

const onDeleteConnection = (id) => {
  const conn = connections.value.find(c => c.id === id)
  if (!conn) return
  pushHistory({
    type: 'DELETE_CONNECTION',
    before: { connection: { ...conn } },
    after: { connection: { ...conn } }
  })
  connections.value = connections.value.filter(c => c.id !== id)
  simulate()
}

const onToggleInput = (gateId) => {
  const gate = gates.value.find(g => g.id === gateId)
  if (gate && gate.type === 'INPUT') {
    gate.outputValue = !gate.outputValue
    simulate()
  }
}

const onLoadTemplate = (template) => {
  const idMap = new Map()
  const newGates = []
  const newConnections = []

  template.gates.forEach((g, index) => {
    gateIdCounter++
    const newGate = {
      id: gateIdCounter,
      type: g.type,
      x: g.x,
      y: g.y,
      inputCount: (g.type === 'NOT' || g.type === 'INPUT' || g.type === 'OUTPUT') ? 1 : 2,
      outputValue: g.type === 'INPUT' ? false : null,
      inputValueA: null,
      inputValueB: null,
      label: g.label || ''
    }
    newGates.push(newGate)
    idMap.set(index, gateIdCounter)
  })

  template.connections.forEach(c => {
    connectionIdCounter++
    newConnections.push({
      id: connectionIdCounter,
      fromGateId: idMap.get(c.fromGateIndex),
      fromPort: c.fromPort || 0,
      toGateId: idMap.get(c.toGateIndex),
      toPort: c.toPort || 0
    })
  })

  undoStack.value = []
  redoStack.value = []
  gates.value = newGates
  connections.value = newConnections
  selectedGateIds.value = []
  simulate()
  MessagePlugin.success(`已加载模板：${template.name}`)
}

const simulate = () => {
  const gateMap = new Map()
  gates.value.forEach(g => gateMap.set(g.id, { ...g }))

  const incomingMap = new Map()
  connections.value.forEach(c => {
    if (!incomingMap.has(c.toGateId)) {
      incomingMap.set(c.toGateId, [])
    }
    incomingMap.get(c.toGateId).push(c)
  })

  const visited = new Set()
  const visiting = new Set()

  const calcGate = (gateId) => {
    const gate = gateMap.get(gateId)
    if (!gate) return false

    if (visited.has(gateId)) {
      return gate.outputValue || false
    }

    if (visiting.has(gateId)) {
      return false
    }

    visiting.add(gateId)

    if (gate.type === 'INPUT') {
      const val = gate.outputValue || false
      gate.outputValue = val
      visited.add(gateId)
      visiting.delete(gateId)
      return val
    }

    const incoming = incomingMap.get(gateId) || []
    let inputA = false
    let inputB = false
    let hasA = false
    let hasB = false

    incoming.forEach(conn => {
      const fromGate = gateMap.get(conn.fromGateId)
      if (fromGate) {
        const val = calcGate(conn.fromGateId)
        const port = conn.toPort != null ? conn.toPort : 0
        if (port === 0) {
          inputA = val
          hasA = true
        } else {
          inputB = val
          hasB = true
        }
      }
    })

    gate.inputValueA = hasA ? inputA : null
    gate.inputValueB = hasB ? inputB : null

    let output = false
    switch (gate.type) {
      case 'AND': output = inputA && inputB; break
      case 'OR': output = inputA || inputB; break
      case 'NOT': output = !inputA; break
      case 'XOR': output = inputA !== inputB; break
      case 'NAND': output = !(inputA && inputB); break
      case 'NOR': output = !(inputA || inputB); break
      case 'OUTPUT': output = inputA; break
      default: output = false
    }

    gate.outputValue = output
    visited.add(gateId)
    visiting.delete(gateId)

    return output
  }

  gates.value.forEach(g => calcGate(g.id))

  gates.value.forEach(g => {
    const computed = gateMap.get(g.id)
    if (computed) {
      g.outputValue = computed.outputValue
      g.inputValueA = computed.inputValueA
      g.inputValueB = computed.inputValueB
    }
  })
}

const undo = () => {
  executeUndo()
}

const redo = () => {
  executeRedo()
}

const onKeyDown = (e) => {
  const ctrlOrMeta = e.ctrlKey || e.metaKey
  if (ctrlOrMeta && e.key.toLowerCase() === 'z' && !e.shiftKey) {
    e.preventDefault()
    undo()
  } else if ((ctrlOrMeta && e.key.toLowerCase() === 'y') || (ctrlOrMeta && e.shiftKey && e.key.toLowerCase() === 'z')) {
    e.preventDefault()
    redo()
  } else if ((e.key === 'Delete' || e.key === 'Backspace') && selectedGateIds.value.length > 0) {
    const activeElement = document.activeElement
    const isInput = activeElement && (activeElement.tagName === 'INPUT' || activeElement.tagName === 'TEXTAREA' || activeElement.isContentEditable)
    if (!isInput) {
      e.preventDefault()
      onDeleteGates([...selectedGateIds.value])
    }
  }
}

watch(currentCircuitId, (newId) => {
  if (newId) {
    loadCircuit(newId)
  }
})

onMounted(async () => {
  document.addEventListener('keydown', onKeyDown)
  try {
    const res = await circuitApi.getAll()
    circuits.value = res.data
    if (circuits.value.length > 0) {
      currentCircuitId.value = circuits.value[0].id
    } else {
      await createCircuit()
    }
  } catch (e) {
    console.error('加载电路列表失败', e)
  }
})

onUnmounted(() => {
  document.removeEventListener('keydown', onKeyDown)
})
</script>

<style scoped>
.app-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--bg-primary);
  color: var(--text-primary);
}

.app-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  background: linear-gradient(180deg, rgba(30, 41, 59, 0.95) 0%, rgba(15, 23, 42, 0.9) 100%);
  border-bottom: 1px solid var(--border-color);
  backdrop-filter: blur(10px);
  position: relative;
  z-index: 10;
}

.app-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--accent-blue), var(--accent-purple), transparent);
  opacity: 0.6;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.logo {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.2), rgba(168, 85, 247, 0.2));
  border: 1px solid rgba(59, 130, 246, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--accent-blue);
  box-shadow: 0 0 20px rgba(59, 130, 246, 0.2);
}

.app-title {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.5px;
  background: linear-gradient(135deg, #60a5fa, #c084fc);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  white-space: nowrap;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.toolbar-group {
  display: flex;
  gap: 4px;
  margin-right: 8px;
  padding-right: 12px;
  border-right: 1px solid var(--border-color);
}

.toolbar-btn {
  display: flex !important;
  align-items: center;
  gap: 4px;
}

.toolbar-btn :deep(svg) {
  flex-shrink: 0;
}

.btn-text {
  font-size: 13px;
}

.app-body {
  flex: 1;
  display: flex;
  overflow: hidden;
  position: relative;
}

.app-body::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, var(--accent-blue) 0%, var(--accent-purple) 50%, var(--accent-blue) 100%);
  opacity: 0.3;
  z-index: 1;
  pointer-events: none;
}
</style>
