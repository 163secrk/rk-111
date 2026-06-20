<template>
  <div class="property-panel" v-if="gate">
    <div class="panel-header">
      <div class="panel-title">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="3" />
          <path d="M12 1v6m0 10v6m11-11h-6M7 12H1" />
        </svg>
        属性面板
      </div>
      <div class="panel-close" @click="onClose">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M18 6L6 18M6 6l12 12" />
        </svg>
      </div>
    </div>

    <div class="panel-content">
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
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  gate: { type: Object, default: null },
  gates: { type: Array, default: () => [] },
  connections: { type: Array, default: () => [] }
})

const emit = defineEmits(['close', 'update-label'])

const getGateTypeName = (type) => {
  const names = {
    AND: '与门',
    OR: '或门',
    NOT: '非门',
    XOR: '异或门',
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

const onClose = () => {
  emit('close')
}

const onLabelInput = (e) => {
  emit('update-label', e.target.value)
}
</script>

<style scoped>
.property-panel {
  width: 280px;
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

.panel-close {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.2s ease;
}

.panel-close:hover {
  background: var(--bg-tertiary);
  color: var(--text-primary);
}

.panel-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
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
.empty-input {
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
.empty-input svg {
  margin-bottom: 8px;
}

.empty-output p,
.empty-input p {
  font-size: 12px;
  margin: 0;
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
</style>
