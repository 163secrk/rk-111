<template>
  <div
    class="logic-canvas"
    ref="canvasRef"
    @dragover.prevent="onDragOver"
    @drop.prevent="onDrop"
    @click="onCanvasClick"
  >
    <div class="canvas-grid" :style="gridStyle"></div>

    <svg class="connections-layer" ref="svgRef">
      <defs>
        <filter id="glow" x="-50%" y="-50%" width="200%" height="200%">
          <feGaussianBlur stdDeviation="2" result="coloredBlur" />
          <feMerge>
            <feMergeNode in="coloredBlur" />
            <feMergeNode in="SourceGraphic" />
          </feMerge>
        </filter>
      </defs>

      <path
        v-for="conn in connections"
        :key="conn.id"
        :d="getConnectionPath(conn)"
        :class="['connection-line', { active: isConnectionActive(conn) }]"
        @contextmenu.prevent.stop="onConnectionContextMenu($event, conn)"
      />

      <path
        v-if="connecting"
        :d="getTempConnectionPath()"
        class="connection-line temp"
      />
    </svg>

    <div
      v-for="gate in gates"
      :key="gate.id"
      :class="['gate-node', { selected: selectedGateId === gate.id }]"
      :style="{
        left: gate.x + 'px',
        top: gate.y + 'px'
      }"
      @mousedown="onGateMouseDown($event, gate)"
    >
      <div :class="['gate-body', gate.type.toLowerCase()]">
        <div class="gate-label">{{ getGateLabel(gate) }}</div>
        <div class="gate-symbol">
          <svg viewBox="0 0 60 40" width="60" height="40">
            <path :d="getGateSymbolPath(gate.type)" fill="none" stroke="currentColor" stroke-width="1.5" />
          </svg>
        </div>
        <div :class="['gate-output', { high: gate.outputValue }]">
          {{ gate.outputValue ? '1' : '0' }}
        </div>
      </div>

      <div class="input-ports">
        <div
          v-for="i in gate.inputCount"
          :key="'in-' + i"
          :class="['port', 'port-input', { active: getPortValue(gate, i - 1) }]"
          :style="{ top: getInputPortTop(i - 1, gate.inputCount) + 'px' }"
          @click.stop="onPortClick(gate.id, 'input', i - 1)"
        >
          <div class="port-dot"></div>
          <div class="port-value">{{ getPortValue(gate, i - 1) ? '1' : '0' }}</div>
        </div>
      </div>

      <div class="output-ports">
        <div
          :class="['port', 'port-output', { active: gate.outputValue }]"
          @click.stop="onPortClick(gate.id, 'output', 0)"
        >
          <div class="port-dot"></div>
        </div>
      </div>

      <div
        v-if="gate.type === 'INPUT'"
        class="toggle-btn"
        @click.stop="onToggleInput(gate.id)"
      >
        {{ gate.outputValue ? 'ON' : 'OFF' }}
      </div>

      <div class="gate-delete" @click.stop="onDeleteGate(gate.id)">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M18 6L6 18M6 6l12 12" />
        </svg>
      </div>
    </div>

    <div
      v-if="contextMenu.visible"
      class="context-menu"
      :style="{ left: contextMenu.x + 'px', top: contextMenu.y + 'px' }"
      @mousedown.stop
      @click.stop
    >
      <div class="context-menu-item danger" @click="onDeleteContextConnection">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M3 6h18M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
        </svg>
        删除连线
      </div>
    </div>

    <div v-if="gates.length === 0" class="empty-hint">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
        <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5" />
      </svg>
      <p>从左侧拖拽逻辑门到画布上开始设计</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'

const props = defineProps({
  gates: { type: Array, default: () => [] },
  connections: { type: Array, default: () => [] }
})

const emit = defineEmits([
  'drop-gate',
  'update-gate',
  'delete-gate',
  'add-connection',
  'delete-connection',
  'toggle-input',
  'select-gate',
  'canvas-click'
])

const canvasRef = ref(null)
const svgRef = ref(null)
const selectedGateId = ref(null)
const connecting = ref(false)
const connectFrom = ref(null)
const mousePos = ref({ x: 0, y: 0 })

const draggingGate = ref(null)
const dragOffset = ref({ x: 0, y: 0 })

const contextMenu = ref({
  visible: false,
  x: 0,
  y: 0,
  targetType: '',
  targetId: null
})

const gridStyle = computed(() => ({
  backgroundSize: '20px 20px, 100px 100px',
  backgroundPosition: '-1px -1px, -1px -1px'
}))

const GATE_WIDTH = 100
const GATE_HEIGHT = 60

const getGateLabel = (gate) => {
  const labels = {
    AND: 'AND',
    OR: 'OR',
    NOT: 'NOT',
    XOR: 'XOR',
    INPUT: 'IN',
    OUTPUT: 'OUT'
  }
  return labels[gate.type] || gate.type
}

const getGateSymbolPath = (type) => {
  const paths = {
    AND: 'M6 8 V32 M6 8 Q36 8 50 20 Q36 32 6 32',
    OR: 'M6 8 Q18 20 6 32 Q30 32 50 20 Q30 8 6 8',
    NOT: 'M6 8 L42 20 L6 32 Z M42 20 L52 20 M48 20 m-3 0 a3 3 0 1 0 6 0 a3 3 0 1 0 -6 0',
    XOR: 'M10 8 Q22 20 10 32 M14 8 Q34 8 50 20 Q34 32 14 32',
    INPUT: 'M8 20 L35 20 M35 14 L52 20 L35 26 Z',
    OUTPUT: 'M52 20 L25 20 M25 14 L8 20 L25 26 Z'
  }
  return paths[type] || paths.AND
}

const getInputPortTop = (index, count) => {
  if (count === 1) return 30
  if (count === 2) return index === 0 ? 18 : 42
  return 20 + index * 15
}

const getPortValue = (gate, portIndex) => {
  if (portIndex === 0) return gate.inputValueA
  if (portIndex === 1) return gate.inputValueB
  return false
}

const getPortPosition = (gateId, portType, portIndex) => {
  const gate = props.gates.find(g => g.id === gateId)
  if (!gate) return { x: 0, y: 0 }

  if (portType === 'output') {
    return {
      x: gate.x + GATE_WIDTH,
      y: gate.y + 30
    }
  } else {
    return {
      x: gate.x,
      y: gate.y + getInputPortTop(portIndex, gate.inputCount) + 5
    }
  }
}

const getConnectionPath = (conn) => {
  const from = getPortPosition(conn.fromGateId, 'output', conn.fromPort || 0)
  const to = getPortPosition(conn.toGateId, 'input', conn.toPort || 0)

  const dx = Math.abs(to.x - from.x)
  const controlOffset = Math.max(dx * 0.5, 50)

  return `M ${from.x} ${from.y} C ${from.x + controlOffset} ${from.y}, ${to.x - controlOffset} ${to.y}, ${to.x} ${to.y}`
}

const isConnectionActive = (conn) => {
  const fromGate = props.gates.find(g => g.id === conn.fromGateId)
  return fromGate && fromGate.outputValue
}

const getTempConnectionPath = () => {
  if (!connectFrom.value) return ''
  const from = getPortPosition(connectFrom.value.gateId, connectFrom.value.portType, connectFrom.value.portIndex)
  const to = mousePos.value

  const dx = Math.abs(to.x - from.x)
  const controlOffset = Math.max(dx * 0.5, 50)

  if (connectFrom.value.portType === 'output') {
    return `M ${from.x} ${from.y} C ${from.x + controlOffset} ${from.y}, ${to.x - controlOffset} ${to.y}, ${to.x} ${to.y}`
  } else {
    return `M ${to.x} ${to.y} C ${to.x + controlOffset} ${to.y}, ${from.x - controlOffset} ${from.y}, ${from.x} ${from.y}`
  }
}

const onDragOver = (e) => {
  e.dataTransfer.dropEffect = 'copy'
}

const onDrop = (e) => {
  const gateType = e.dataTransfer.getData('gateType')
  if (!gateType) return

  const rect = canvasRef.value.getBoundingClientRect()
  const x = e.clientX - rect.left - GATE_WIDTH / 2
  const y = e.clientY - rect.top - GATE_HEIGHT / 2

  emit('drop-gate', { type: gateType, x: Math.max(0, x), y: Math.max(0, y) })
}

const onCanvasClick = () => {
  selectedGateId.value = null
  hideContextMenu()
  emit('canvas-click')
  if (connecting.value) {
    connecting.value = false
    connectFrom.value = null
  }
}

const onGateMouseDown = (e, gate) => {
  if (e.target.closest('.port') || e.target.closest('.toggle-btn') || e.target.closest('.gate-delete')) {
    return
  }

  selectedGateId.value = gate.id
  emit('select-gate', gate)
  draggingGate.value = gate

  const rect = canvasRef.value.getBoundingClientRect()
  dragOffset.value = {
    x: e.clientX - rect.left - gate.x,
    y: e.clientY - rect.top - gate.y
  }

  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
}

const onMouseMove = (e) => {
  const rect = canvasRef.value.getBoundingClientRect()
  mousePos.value = {
    x: e.clientX - rect.left,
    y: e.clientY - rect.top
  }

  if (draggingGate.value) {
    const newX = e.clientX - rect.left - dragOffset.value.x
    const newY = e.clientY - rect.top - dragOffset.value.y

    emit('update-gate', {
      id: draggingGate.value.id,
      x: Math.max(0, newX),
      y: Math.max(0, newY)
    })
  }
}

const onMouseUp = () => {
  draggingGate.value = null
  document.removeEventListener('mousemove', onMouseMove)
  document.removeEventListener('mouseup', onMouseUp)
}

const onPortClick = (gateId, portType, portIndex) => {
  if (!connecting.value) {
    connecting.value = true
    connectFrom.value = { gateId, portType, portIndex }
  } else {
    const from = connectFrom.value

    if (from.portType === 'output' && portType === 'input' && from.gateId !== gateId) {
      emit('add-connection', {
        fromGateId: from.gateId,
        fromPort: from.portIndex,
        toGateId: gateId,
        toPort: portIndex
      })
    } else if (from.portType === 'input' && portType === 'output' && from.gateId !== gateId) {
      emit('add-connection', {
        fromGateId: gateId,
        fromPort: portIndex,
        toGateId: from.gateId,
        toPort: from.portIndex
      })
    }

    connecting.value = false
    connectFrom.value = null
  }
}

const onConnectionContextMenu = (e, conn) => {
  const rect = canvasRef.value.getBoundingClientRect()
  contextMenu.value = {
    visible: true,
    x: e.clientX - rect.left,
    y: e.clientY - rect.top,
    targetType: 'connection',
    targetId: conn.id
  }
}

const hideContextMenu = () => {
  contextMenu.value.visible = false
}

const onDeleteContextConnection = () => {
  if (contextMenu.value.targetType === 'connection') {
    emit('delete-connection', contextMenu.value.targetId)
  }
  hideContextMenu()
}

const onDeleteGate = (id) => {
  emit('delete-gate', id)
}

const onToggleInput = (gateId) => {
  emit('toggle-input', gateId)
}

const onDocumentMouseDown = (e) => {
  if (e.button === 0) {
    hideContextMenu()
  }
}

onMounted(() => {
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mousedown', onDocumentMouseDown)
})

onUnmounted(() => {
  document.removeEventListener('mousemove', onMouseMove)
  document.removeEventListener('mouseup', onMouseUp)
  document.removeEventListener('mousedown', onDocumentMouseDown)
})
</script>

<style scoped>
.logic-canvas {
  flex: 1;
  position: relative;
  overflow: auto;
  background: var(--bg-primary);
  cursor: default;
}

.canvas-grid {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image:
    linear-gradient(rgba(71, 85, 105, 0.15) 1px, transparent 1px),
    linear-gradient(90deg, rgba(71, 85, 105, 0.15) 1px, transparent 1px),
    radial-gradient(circle, rgba(59, 130, 246, 0.15) 1.5px, transparent 1.5px);
  background-size: 20px 20px, 20px 20px, 100px 100px;
  background-position: -1px -1px, -1px -1px, 0 0;
  pointer-events: none;
}

.connections-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.connection-line {
  fill: none;
  stroke: var(--border-color);
  stroke-width: 2.5;
  pointer-events: stroke;
  cursor: pointer;
  transition: stroke 0.2s ease;
}

.connection-line:hover {
  stroke: var(--accent-blue);
  stroke-width: 3;
}

.connection-line.active {
  stroke: var(--signal-high);
  filter: url(#glow);
}

.connection-line.temp {
  stroke: var(--accent-blue);
  stroke-dasharray: 6 4;
  pointer-events: none;
}

.gate-node {
  position: absolute;
  width: 100px;
  height: 60px;
  cursor: move;
  user-select: none;
}

.gate-node.selected .gate-body {
  border-color: var(--accent-blue);
  box-shadow: 0 0 20px rgba(59, 130, 246, 0.4);
}

.gate-body {
  position: absolute;
  left: 15px;
  top: 0;
  width: 70px;
  height: 60px;
  background: linear-gradient(145deg, var(--bg-secondary), var(--bg-tertiary));
  border: 1.5px solid var(--border-color);
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 
    0 4px 6px -1px rgba(0, 0, 0, 0.3),
    0 2px 4px -1px rgba(0, 0, 0, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.05);
}

.gate-body:hover {
  border-color: var(--accent-blue);
  box-shadow: 
    0 8px 25px -5px rgba(59, 130, 246, 0.3),
    0 4px 10px -2px rgba(59, 130, 246, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
  transform: translateY(-1px);
}

.gate-body.and {
  color: var(--accent-blue);
}

.gate-body.or {
  color: var(--accent-green);
}

.gate-body.not {
  color: var(--accent-orange);
}

.gate-body.xor {
  color: var(--accent-purple);
}

.gate-body.input {
  color: var(--signal-high);
}

.gate-body.output {
  color: var(--accent-red);
}

.gate-label {
  font-size: 10px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 2px;
}

.gate-symbol {
  display: flex;
  align-items: center;
  justify-content: center;
}

.gate-output {
  position: absolute;
  bottom: 4px;
  right: 6px;
  font-size: 9px;
  font-weight: 700;
  color: var(--text-secondary);
}

.gate-output.high {
  color: var(--signal-high);
  text-shadow: 0 0 6px var(--signal-high);
}

.input-ports {
  position: absolute;
  left: 0;
  top: 0;
  width: 15px;
  height: 100%;
}

.output-ports {
  position: absolute;
  right: 0;
  top: 0;
  width: 15px;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.port {
  position: absolute;
  width: 10px;
  height: 10px;
  cursor: crosshair;
  z-index: 10;
}

.port-input {
  left: -5px;
}

.port-output {
  right: -5px;
}

.port-dot {
  position: absolute;
  top: 0;
  left: 0;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--bg-tertiary);
  border: 2px solid var(--border-color);
  transition: all 0.2s ease;
  box-sizing: border-box;
}

.port:hover .port-dot {
  background: var(--accent-blue);
  border-color: var(--accent-blue);
  transform: scale(1.2);
}

.port.active .port-dot {
  background: var(--signal-high);
  border-color: var(--signal-high);
  box-shadow: 0 0 8px var(--signal-high);
}

.port-value {
  position: absolute;
  left: 18px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 9px;
  color: var(--text-secondary);
  font-weight: 600;
  white-space: nowrap;
}

.toggle-btn {
  position: absolute;
  top: -22px;
  left: 50%;
  transform: translateX(-50%);
  padding: 2px 8px;
  font-size: 9px;
  font-weight: 700;
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.2s ease;
}

.toggle-btn:hover {
  background: var(--accent-blue);
  color: white;
  border-color: var(--accent-blue);
}

.gate-delete {
  position: absolute;
  top: -8px;
  right: -8px;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: var(--accent-red);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.2s ease;
  z-index: 20;
}

.gate-node:hover .gate-delete {
  opacity: 1;
}

.empty-hint {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  color: var(--text-secondary);
  pointer-events: none;
}

.empty-hint svg {
  margin-bottom: 12px;
  opacity: 0.5;
}

.empty-hint p {
  font-size: 14px;
}

.context-menu {
  position: absolute;
  min-width: 140px;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 4px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.3);
  z-index: 1000;
  animation: fadeIn 0.15s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-4px); }
  to { opacity: 1; transform: translateY(0); }
}

.context-menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  font-size: 13px;
  color: var(--text-primary);
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.15s ease;
}

.context-menu-item:hover {
  background: var(--bg-tertiary);
}

.context-menu-item.danger {
  color: var(--accent-red);
}

.context-menu-item.danger:hover {
  background: rgba(239, 68, 68, 0.1);
}
</style>
