<template>
  <div class="gate-panel">
    <div class="panel-title">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <rect x="3" y="3" width="7" height="7" />
        <rect x="14" y="3" width="7" height="7" />
        <rect x="3" y="14" width="7" height="7" />
        <rect x="14" y="14" width="7" height="7" />
      </svg>
      逻辑门组件
    </div>

    <div class="gate-list">
      <div
        v-for="gate in gateTypes"
        :key="gate.type"
        class="gate-item"
        draggable="true"
        @dragstart="onDragStart($event, gate.type)"
      >
        <div class="gate-icon" :class="gate.type.toLowerCase()">
          <svg :viewBox="gate.svgViewBox" width="40" height="32">
            <path :d="gate.svgPath" fill="none" stroke="currentColor" stroke-width="1.5" />
          </svg>
        </div>
        <div class="gate-info">
          <div class="gate-name">{{ gate.name }}</div>
          <div class="gate-desc">{{ gate.desc }}</div>
        </div>
      </div>
    </div>

    <div class="panel-title" style="margin-top: 20px;">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="3" />
        <path d="M12 1v6m0 10v6m11-11h-6M7 12H1" />
      </svg>
      输入/输出
    </div>

    <div class="gate-list">
      <div
        v-for="gate in ioGates"
        :key="gate.type"
        class="gate-item"
        draggable="true"
        @dragstart="onDragStart($event, gate.type)"
      >
        <div class="gate-icon" :class="gate.type.toLowerCase()">
          <svg viewBox="0 0 40 32" width="40" height="32">
            <path :d="gate.svgPath" fill="none" stroke="currentColor" stroke-width="1.5" />
          </svg>
        </div>
        <div class="gate-info">
          <div class="gate-name">{{ gate.name }}</div>
          <div class="gate-desc">{{ gate.desc }}</div>
        </div>
      </div>
    </div>

    <div class="panel-title" style="margin-top: 20px;">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
        <polyline points="14 2 14 8 20 8" />
        <line x1="9" y1="13" x2="15" y2="13" />
        <line x1="9" y1="17" x2="15" y2="17" />
      </svg>
      经典电路模板
    </div>

    <div class="template-list">
      <div
        v-for="template in templates"
        :key="template.id"
        class="template-item"
        @click="onLoadTemplate(template)"
      >
        <div class="template-icon" :style="{ background: template.color }">
          {{ template.icon }}
        </div>
        <div class="template-info">
          <div class="template-name">{{ template.name }}</div>
          <div class="template-desc">{{ template.description }}</div>
        </div>
        <div class="template-action">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M5 12h14M12 5l7 7-7 7" />
          </svg>
        </div>
      </div>
    </div>

    <div class="panel-tip">
      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="10" />
        <path d="M12 16v-4M12 8h.01" />
      </svg>
      拖拽组件到画布，点击模板一键加载
    </div>
  </div>
</template>

<script setup>
import { circuitTemplates } from '../circuitTemplates'

const gateTypes = [
  {
    type: 'AND',
    name: '与门',
    desc: 'AND Gate',
    svgViewBox: '0 0 40 32',
    svgPath: 'M4 8 V24 M4 8 Q20 8 28 16 Q20 24 4 24'
  },
  {
    type: 'OR',
    name: '或门',
    desc: 'OR Gate',
    svgViewBox: '0 0 40 32',
    svgPath: 'M4 8 Q12 16 4 24 Q16 24 28 16 Q16 8 4 8'
  },
  {
    type: 'NOT',
    name: '非门',
    desc: 'NOT Gate',
    svgViewBox: '0 0 40 32',
    svgPath: 'M4 8 L24 16 L4 24 Z M24 16 L32 16 M28 16 m-2 0 a2 2 0 1 0 4 0 a2 2 0 1 0 -4 0'
  },
  {
    type: 'XOR',
    name: '异或门',
    desc: 'XOR Gate',
    svgViewBox: '0 0 40 32',
    svgPath: 'M7 8 Q15 16 7 24 M10 8 Q22 8 30 16 Q22 24 10 24'
  },
  {
    type: 'NAND',
    name: '与非门',
    desc: 'NAND Gate',
    svgViewBox: '0 0 40 32',
    svgPath: 'M4 8 V24 M4 8 Q20 8 28 16 Q20 24 4 24 M28 16 L32 16 M30 16 m-2 0 a2 2 0 1 0 4 0 a2 2 0 1 0 -4 0'
  },
  {
    type: 'NOR',
    name: '或非门',
    desc: 'NOR Gate',
    svgViewBox: '0 0 40 32',
    svgPath: 'M4 8 Q12 16 4 24 Q16 24 28 16 Q16 8 4 8 M28 16 L32 16 M30 16 m-2 0 a2 2 0 1 0 4 0 a2 2 0 1 0 -4 0'
  }
]

const ioGates = [
  {
    type: 'INPUT',
    name: '输入信号',
    desc: 'Input',
    svgPath: 'M6 16 L20 16 M20 12 L28 16 L20 20 Z'
  },
  {
    type: 'OUTPUT',
    name: '输出显示',
    desc: 'Output',
    svgPath: 'M34 16 L20 16 M20 12 L12 16 L20 20 Z'
  }
]

const templateColors = [
  'linear-gradient(135deg, #3b82f6, #60a5fa)',
  'linear-gradient(135deg, #8b5cf6, #a78bfa)',
  'linear-gradient(135deg, #f97316, #fb923c)',
  'linear-gradient(135deg, #10b981, #34d399)'
]

const templates = circuitTemplates.map((t, i) => ({
  ...t,
  color: templateColors[i % templateColors.length]
}))

const emit = defineEmits(['drag-start', 'load-template'])

const onDragStart = (e, type) => {
  e.dataTransfer.setData('gateType', type)
  e.dataTransfer.effectAllowed = 'copy'
  emit('drag-start', type)
}

const onLoadTemplate = (template) => {
  emit('load-template', template)
}
</script>

<style scoped>
.gate-panel {
  width: 240px;
  height: 100%;
  background: var(--bg-secondary);
  border-right: 1px solid var(--border-color);
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 12px;
  padding-left: 4px;
}

.gate-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.gate-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  cursor: grab;
  transition: all 0.2s ease;
}

.gate-item:hover {
  border-color: var(--accent-blue);
  background: rgba(59, 130, 246, 0.1);
  transform: translateX(2px);
}

.gate-item:active {
  cursor: grabbing;
}

.gate-icon {
  width: 48px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  background: var(--bg-secondary);
}

.gate-icon.and {
  color: var(--accent-blue);
}

.gate-icon.or {
  color: var(--accent-green);
}

.gate-icon.not {
  color: var(--accent-orange);
}

.gate-icon.xor {
  color: var(--accent-purple);
}

.gate-icon.nand {
  color: #ec4899;
}

.gate-icon.nor {
  color: #14b8a6;
}

.gate-icon.input {
  color: var(--signal-high);
}

.gate-icon.output {
  color: var(--accent-red);
}

.gate-info {
  flex: 1;
}

.gate-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.gate-desc {
  font-size: 11px;
  color: var(--text-secondary);
  margin-top: 2px;
}

.panel-tip {
  margin-top: auto;
  padding: 10px 12px;
  background: rgba(59, 130, 246, 0.1);
  border: 1px solid rgba(59, 130, 246, 0.3);
  border-radius: 8px;
  font-size: 12px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 6px;
}

.gate-panel::-webkit-scrollbar {
  width: 6px;
}

.gate-panel::-webkit-scrollbar-track {
  background: transparent;
}

.template-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.template-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.template-item:hover {
  border-color: var(--accent-purple);
  background: rgba(168, 85, 247, 0.1);
  transform: translateX(2px);
}

.template-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  color: white;
  font-weight: 700;
  font-size: 13px;
  flex-shrink: 0;
}

.template-info {
  flex: 1;
  min-width: 0;
}

.template-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.template-desc {
  font-size: 11px;
  color: var(--text-secondary);
  margin-top: 2px;
}

.template-action {
  color: var(--text-secondary);
  transition: all 0.2s ease;
}

.template-item:hover .template-action {
  color: var(--accent-purple);
  transform: translateX(2px);
}

.gate-panel::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 3px;
}
</style>
