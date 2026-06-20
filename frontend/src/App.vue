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
        <t-select v-model="currentCircuitId" placeholder="选择电路" style="width: 200px; margin-right: 12px;">
          <t-option v-for="c in circuits" :key="c.id" :value="c.id" :label="c.name" />
        </t-select>
        <t-button theme="primary" @click="createCircuit">新建电路</t-button>
        <t-button theme="default" @click="saveCircuit" style="margin-left: 8px;">保存</t-button>
      </div>
    </header>

    <div class="app-body">
      <GatePanel @drag-start="onDragStart" />
      <LogicCanvas
        ref="canvasRef"
        :gates="gates"
        :connections="connections"
        @drop-gate="onDropGate"
        @update-gate="onUpdateGate"
        @delete-gate="onDeleteGate"
        @add-connection="onAddConnection"
        @delete-connection="onDeleteConnection"
        @toggle-input="onToggleInput"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import GatePanel from './components/GatePanel.vue'
import LogicCanvas from './components/LogicCanvas.vue'
import { circuitApi } from './api'

const circuits = ref([])
const currentCircuitId = ref(null)
const gates = ref([])
const connections = ref([])
const canvasRef = ref(null)
let gateIdCounter = 0
let connectionIdCounter = 0

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
  } catch (e) {
    MessagePlugin.error('加载电路失败')
  }
}

const onDragStart = (gateType) => {
  // handled by HTML5 drag and drop
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
  simulate()
}

const onUpdateGate = ({ id, x, y }) => {
  const gate = gates.value.find(g => g.id === id)
  if (gate) {
    gate.x = x
    gate.y = y
  }
}

const onDeleteGate = (id) => {
  gates.value = gates.value.filter(g => g.id !== id)
  connections.value = connections.value.filter(c => c.fromGateId !== id && c.toGateId !== id)
  simulate()
}

const onAddConnection = ({ fromGateId, fromPort, toGateId, toPort }) => {
  const exists = connections.value.some(
    c => c.toGateId === toGateId && c.toPort === toPort
  )
  if (exists) {
    return
  }
  connectionIdCounter++
  connections.value.push({
    id: connectionIdCounter,
    fromGateId,
    fromPort,
    toGateId,
    toPort
  })
  simulate()
}

const onDeleteConnection = (id) => {
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

watch(currentCircuitId, (newId) => {
  if (newId) {
    loadCircuit(newId)
  }
})

onMounted(async () => {
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
