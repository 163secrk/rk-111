<template>
  <div class="property-panel" @click.stop>
    <div class="panel-header">
      <div class="panel-title">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="3" />
          <path d="M12 1v6m0 10v6m11-11h-6M7 12H1" />
        </svg>
        右侧面板
      </div>
    </div>

    <div class="tabs">
      <div
        class="tab-item"
        :class="{ active: activeTab === 'property' }"
        @click="activeTab = 'property'"
      >
        属性
      </div>
      <div
        class="tab-item"
        :class="{ active: activeTab === 'truth-table' }"
        @click="activeTab = 'truth-table'"
      >
        真值表
      </div>
    </div>

    <div class="panel-content">
      <div v-if="activeTab === 'property'" class="tab-content">
        <template v-if="gate">
          <div class="property-section">
            <div class="section-title">基本信息</div>
            
            <div class="property-item">
              <label>类型</label>
              <div class="property-value gate-type" :class="gate.type.toLowerCase()">
                <span class="type-dot"></span>
                {{ getGateTypeName(gate.type) }}
              </div>
            </div>

            <div class="property-item">
              <label>标签名称</label>
              <input
                type="text"
                class="property-input"
                :value="gate.label"
                @input="onLabelInput"
                placeholder="请输入标签名称"
              />
            </div>
          </div>

          <div class="property-section">
            <div class="section-title">端口信息</div>
            
            <div class="property-item">
              <label>输入数量</label>
              <div class="property-value">
                <span class="port-count">{{ gate.inputCount }}</span>
                <span class="port-unit">个</span>
              </div>
            </div>

            <div class="property-item">
              <label>输出数量</label>
              <div class="property-value">
                <span class="port-count">1</span>
                <span class="port-unit">个</span>
              </div>
            </div>
          </div>

          <div class="property-section">
            <div class="section-title">输出去向</div>
            
            <div v-if="outputTargets.length > 0" class="output-list">
              <div
                v-for="target in outputTargets"
                :key="target.id"
                class="output-target-item"
              >
                <div class="target-icon" :class="target.type.toLowerCase()">
                  <svg viewBox="0 0 40 32" width="28" height="22">
                    <path :d="getGateSymbolPath(target.type)" fill="none" stroke="currentColor" stroke-width="1.5" />
                  </svg>
                </div>
                <div class="target-info">
                  <div class="target-name">{{ target.label || getGateTypeName(target.type) }}</div>
                  <div class="target-port">输入端口 {{ target.toPort + 1 }}</div>
                </div>
              </div>
            </div>
            <div v-else class="empty-output">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z" />
              </svg>
              <p>暂无输出连接</p>
            </div>
          </div>

          <div class="property-section">
            <div class="section-title">输入来源</div>
            
            <div v-if="inputSources.length > 0" class="input-list">
              <div
                v-for="source in inputSources"
                :key="source.id"
                class="input-source-item"
              >
                <div class="source-icon" :class="source.type.toLowerCase()">
                  <svg viewBox="0 0 40 32" width="28" height="22">
                    <path :d="getGateSymbolPath(source.type)" fill="none" stroke="currentColor" stroke-width="1.5" />
                  </svg>
                </div>
                <div class="source-info">
                  <div class="source-name">{{ source.label || getGateTypeName(source.type) }}</div>
                  <div class="source-port">输出端口</div>
                </div>
              </div>
            </div>
            <div v-else class="empty-input">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M11 2L21 14h-9l1 8 10-12h-9l1-8z" />
              </svg>
              <p>暂无输入连接</p>
            </div>
          </div>
        </template>

        <div v-else class="empty-selection">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M4 7V4h16v3M9 20h6M12 4v16" />
          </svg>
          <p>请选择一个门组件查看属性</p>
        </div>
      </div>

      <div v-if="activeTab === 'truth-table'" class="tab-content truth-table-content">
        <div class="truth-table-header">
          <div class="section-title">真值表生成</div>
          <div class="truth-table-info">
            <span>输入端口: {{ inputGates.length }} 个</span>
            <span>输出端口: {{ outputGates.length }} 个</span>
            <span>组合数: {{ combinationCount }}</span>
          </div>
        </div>

        <t-button
          theme="primary"
          block
          :disabled="inputGates.length === 0 || outputGates.length === 0 || inputGates.length > 8"
          @click="generateTruthTable"
          class="generate-btn"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
            <polyline points="14 2 14 8 20 8" />
            <line x1="9" y1="15" x2="15" y2="15" />
          </svg>
          生成真值表
        </t-button>

        <div v-if="inputGates.length > 8" class="warning-tip">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
            <line x1="12" y1="9" x2="12" y2="13" />
            <line x1="12" y1="17" x2="12.01" y2="17" />
          </svg>
          <span>组合数过大</span>
        </div>

        <div v-if="inputGates.length === 0 || outputGates.length === 0" class="empty-tip">
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M9 12l2 2 4-4" />
            <path d="M21 12c0 4.97-4.03 9-9 9s-9-4.03-9-9 4.03-9 9-9c2.35 0 4.47.9 6 2.35" />
          </svg>
          <p>请在画布中添加输入和输出端口</p>
        </div>

        <div v-if="truthTableData.length > 0" class="truth-table-wrapper">
          <div class="table-container">
            <table class="truth-table">
              <thead>
                <tr>
                  <th
                    v-for="col in tableHeaders"
                    :key="col.key"
                    :class="{ 'input-col': col.isInput, 'output-col': !col.isInput }"
                  >
                    {{ col.label }}
                  </th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(row, index) in truthTableData" :key="index">
                  <td
                    v-for="col in tableHeaders"
                    :key="col.key"
                    :class="{
                      'value-high': row[col.key],
                      'value-low': !row[col.key],
                      'input-col': col.isInput,
                      'output-col': !col.isInput
                    }"
                  >
                    {{ row[col.key] ? '1' : '0' }}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  gate: { type: Object, default: null },
  gates: { type: Array, default: () => [] },
  connections: { type: Array, default: () => [] }
})

const emit = defineEmits(['close', 'update-label'])

const activeTab = ref('property')
const truthTableData = ref([])

const getGateTypeName = (type) => {
  const names = {
    AND: '与门',
    OR: '或门',
    NOT: '非门',
    XOR: '异或门',
    NAND: '与非门',
    NOR: '或非门',
    INPUT: '输入信号',
    OUTPUT: '输出显示'
  }
  return names[type] || type
}

const getGateSymbolPath = (type) => {
  const paths = {
    AND: 'M4 8 V24 M4 8 Q20 8 28 16 Q20 24 4 24',
    OR: 'M4 8 Q12 16 4 24 Q16 24 28 16 Q16 8 4 8',
    NOT: 'M4 8 L24 16 L4 24 Z M24 16 L32 16 M28 16 m-2 0 a2 2 0 1 0 4 0 a2 2 0 1 0 -4 0',
    XOR: 'M7 8 Q15 16 7 24 M10 8 Q22 8 30 16 Q22 24 10 24',
    NAND: 'M4 8 V24 M4 8 Q20 8 28 16 Q20 24 4 24 M28 16 L32 16 M30 16 m-2 0 a2 2 0 1 0 4 0 a2 2 0 1 0 -4 0',
    NOR: 'M4 8 Q12 16 4 24 Q16 24 28 16 Q16 8 4 8 M28 16 L32 16 M30 16 m-2 0 a2 2 0 1 0 4 0 a2 2 0 1 0 -4 0',
    INPUT: 'M6 16 L20 16 M20 12 L28 16 L20 20 Z',
    OUTPUT: 'M34 16 L20 16 M20 12 L12 16 L20 20 Z'
  }
  return paths[type] || paths.AND
}

const outputTargets = computed(() => {
  if (!props.gate) return []
  const targets = []
  props.connections.forEach(conn => {
    if (conn.fromGateId === props.gate.id) {
      const targetGate = props.gates.find(g => g.id === conn.toGateId)
      if (targetGate) {
        targets.push({
          ...targetGate,
          toPort: conn.toPort != null ? conn.toPort : 0
        })
      }
    }
  })
  return targets
})

const inputSources = computed(() => {
  if (!props.gate) return []
  const sources = []
  props.connections.forEach(conn => {
    if (conn.toGateId === props.gate.id) {
      const sourceGate = props.gates.find(g => g.id === conn.fromGateId)
      if (sourceGate) {
        sources.push({
          ...sourceGate,
          fromPort: conn.fromPort != null ? conn.fromPort : 0
        })
      }
    }
  })
  return sources
})

const inputGates = computed(() => {
  return props.gates
    .filter(g => g.type === 'INPUT')
    .map((g, index) => ({
      ...g,
      displayName: g.label || `输入${index + 1}`
    }))
})

const outputGates = computed(() => {
  return props.gates
    .filter(g => g.type === 'OUTPUT')
    .map((g, index) => ({
      ...g,
      displayName: g.label || `输出${index + 1}`
    }))
})

const combinationCount = computed(() => {
  const n = inputGates.value.length
  if (n === 0) return 0
  if (n > 8) return '2^' + n
  return Math.pow(2, n)
})

const tableHeaders = computed(() => {
  const headers = []
  inputGates.value.forEach(g => {
    headers.push({ key: `input_${g.id}`, label: g.displayName, isInput: true })
  })
  outputGates.value.forEach(g => {
    headers.push({ key: `output_${g.id}`, label: g.displayName, isInput: false })
  })
  return headers
})

const calculateOutput = (inputValues) => {
  const gateMap = new Map()
  props.gates.forEach(g => gateMap.set(g.id, { ...g }))

  const incomingMap = new Map()
  props.connections.forEach(c => {
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
      const val = inputValues[gateId] != null ? inputValues[gateId] : false
      gate.outputValue = val
      visited.add(gateId)
      visiting.delete(gateId)
      return val
    }

    const incoming = incomingMap.get(gateId) || []
    let inputA = false
    let inputB = false

    incoming.forEach(conn => {
      const fromGate = gateMap.get(conn.fromGateId)
      if (fromGate) {
        const val = calcGate(conn.fromGateId)
        const port = conn.toPort != null ? conn.toPort : 0
        if (port === 0) {
          inputA = val
        } else {
          inputB = val
        }
      }
    })

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

  outputGates.value.forEach(g => calcGate(g.id))

  const results = {}
  outputGates.value.forEach(g => {
    const computed = gateMap.get(g.id)
    results[g.id] = computed ? computed.outputValue : false
  })

  return results
}

const generateTruthTable = () => {
  if (inputGates.value.length === 0 || outputGates.value.length === 0) return
  if (inputGates.value.length > 8) return

  const n = inputGates.value.length
  const total = Math.pow(2, n)
  const rows = []

  for (let i = 0; i < total; i++) {
    const inputValues = {}
    const row = {}

    inputGates.value.forEach((g, idx) => {
      const val = Boolean((i >> (n - 1 - idx)) & 1)
      inputValues[g.id] = val
      row[`input_${g.id}`] = val
    })

    const outputValues = calculateOutput(inputValues)
    outputGates.value.forEach(g => {
      row[`output_${g.id}`] = outputValues[g.id] || false
    })

    rows.push(row)
  }

  truthTableData.value = rows
}

const onLabelInput = (e) => {
  emit('update-label', e.target.value)
}
</script>

<style scoped>
.property-panel {
  width: 320px;
  height: 100%;
  background: var(--bg-secondary);
  border-left: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid var(--border-color);
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.tabs {
  display: flex;
  border-bottom: 1px solid var(--border-color);
  padding: 0 8px;
}

.tab-item {
  flex: 1;
  padding: 10px 0;
  text-align: center;
  font-size: 13px;
  color: var(--text-secondary);
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.2s ease;
}

.tab-item:hover {
  color: var(--text-primary);
}

.tab-item.active {
  color: var(--accent-blue);
  border-bottom-color: var(--accent-blue);
}

.panel-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.tab-content {
  height: 100%;
}

.property-section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 12px;
  padding-left: 4px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.property-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 12px;
}

.property-item label {
  font-size: 12px;
  color: var(--text-secondary);
}

.property-value {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: var(--text-primary);
}

.gate-type {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.type-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: currentColor;
}

.gate-type.and { color: var(--accent-blue); }
.gate-type.or { color: var(--accent-green); }
.gate-type.not { color: var(--accent-orange); }
.gate-type.xor { color: var(--accent-purple); }
.gate-type.nand { color: #ec4899; }
.gate-type.nor { color: #14b8a6; }
.gate-type.input { color: var(--signal-high); }
.gate-type.output { color: var(--accent-red); }

.property-input {
  width: 100%;
  padding: 8px 12px;
  font-size: 13px;
  color: var(--text-primary);
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  outline: none;
  transition: all 0.2s ease;
  box-sizing: border-box;
}

.property-input:focus {
  border-color: var(--accent-blue);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15);
}

.port-count {
  font-size: 20px;
  font-weight: 700;
  color: var(--accent-blue);
}

.port-unit {
  font-size: 12px;
  color: var(--text-secondary);
}

.output-list,
.input-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.output-target-item,
.input-source-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  transition: all 0.2s ease;
}

.output-target-item:hover,
.input-source-item:hover {
  border-color: var(--accent-blue);
  background: rgba(59, 130, 246, 0.05);
}

.target-icon,
.source-icon {
  width: 36px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  background: var(--bg-secondary);
}

.target-icon.and,
.source-icon.and { color: var(--accent-blue); }
.target-icon.or,
.source-icon.or { color: var(--accent-green); }
.target-icon.not,
.source-icon.not { color: var(--accent-orange); }
.target-icon.xor,
.source-icon.xor { color: var(--accent-purple); }
.target-icon.nand,
.source-icon.nand { color: #ec4899; }
.target-icon.nor,
.source-icon.nor { color: #14b8a6; }
.target-icon.input,
.source-icon.input { color: var(--signal-high); }
.target-icon.output,
.source-icon.output { color: var(--accent-red); }

.target-info,
.source-info {
  flex: 1;
  min-width: 0;
}

.target-name,
.source-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.target-port,
.source-port {
  font-size: 11px;
  color: var(--text-secondary);
  margin-top: 2px;
}

.empty-output,
.empty-input,
.empty-selection,
.empty-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px;
  text-align: center;
  color: var(--text-secondary);
  opacity: 0.6;
}

.empty-output svg,
.empty-input svg,
.empty-selection svg,
.empty-tip svg {
  margin-bottom: 8px;
}

.empty-output p,
.empty-input p,
.empty-selection p,
.empty-tip p {
  font-size: 12px;
  margin: 0;
}

.empty-selection {
  padding-top: 40px;
}

.truth-table-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.truth-table-header {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.truth-table-info {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 12px;
  color: var(--text-secondary);
}

.generate-btn {
  display: flex !important;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.warning-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  background: rgba(249, 115, 22, 0.1);
  border: 1px solid rgba(249, 115, 22, 0.3);
  border-radius: 8px;
  font-size: 12px;
  color: var(--accent-orange);
}

.truth-table-wrapper {
  margin-top: 8px;
}

.table-container {
  max-height: 400px;
  overflow-y: auto;
  overflow-x: auto;
  border: 1px solid var(--border-color);
  border-radius: 8px;
}

.truth-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.truth-table th,
.truth-table td {
  padding: 8px 12px;
  text-align: center;
  border-bottom: 1px solid var(--border-color);
}

.truth-table th {
  background: var(--bg-tertiary);
  font-weight: 600;
  color: var(--text-primary);
  position: sticky;
  top: 0;
  z-index: 1;
}

.truth-table th.input-col {
  color: var(--accent-blue);
}

.truth-table th.output-col {
  color: var(--accent-green);
}

.truth-table tbody tr:hover {
  background: rgba(59, 130, 246, 0.05);
}

.truth-table td.input-col {
  background: rgba(59, 130, 246, 0.03);
}

.truth-table td.output-col {
  background: rgba(34, 197, 94, 0.03);
}

.value-high {
  color: var(--signal-high);
  font-weight: 600;
}

.value-low {
  color: var(--signal-low);
}

.panel-content::-webkit-scrollbar {
  width: 6px;
}

.panel-content::-webkit-scrollbar-track {
  background: transparent;
}

.panel-content::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 3px;
}

.table-container::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.table-container::-webkit-scrollbar-track {
  background: transparent;
}

.table-container::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 3px;
}
</style>
