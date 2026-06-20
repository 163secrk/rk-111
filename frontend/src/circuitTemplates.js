export const circuitTemplates = [
  {
    id: 'half-adder',
    name: '半加器',
    description: 'Half Adder',
    icon: '⊕',
    gates: [
      { type: 'INPUT', x: 60, y: 80, label: 'A' },
      { type: 'INPUT', x: 60, y: 200, label: 'B' },
      { type: 'XOR', x: 250, y: 100, label: '' },
      { type: 'AND', x: 250, y: 220, label: '' },
      { type: 'OUTPUT', x: 440, y: 100, label: 'Sum' },
      { type: 'OUTPUT', x: 440, y: 220, label: 'Cout' }
    ],
    connections: [
      { fromGateIndex: 0, fromPort: 0, toGateIndex: 2, toPort: 0 },
      { fromGateIndex: 1, fromPort: 0, toGateIndex: 2, toPort: 1 },
      { fromGateIndex: 0, fromPort: 0, toGateIndex: 3, toPort: 0 },
      { fromGateIndex: 1, fromPort: 0, toGateIndex: 3, toPort: 1 },
      { fromGateIndex: 2, fromPort: 0, toGateIndex: 4, toPort: 0 },
      { fromGateIndex: 3, fromPort: 0, toGateIndex: 5, toPort: 0 }
    ]
  },
  {
    id: 'full-adder',
    name: '全加器',
    description: 'Full Adder',
    icon: 'Σ',
    gates: [
      { type: 'INPUT', x: 50, y: 60, label: 'A' },
      { type: 'INPUT', x: 50, y: 160, label: 'B' },
      { type: 'INPUT', x: 50, y: 260, label: 'Cin' },
      { type: 'XOR', x: 220, y: 90, label: '' },
      { type: 'XOR', x: 390, y: 90, label: '' },
      { type: 'AND', x: 220, y: 210, label: '' },
      { type: 'AND', x: 390, y: 210, label: '' },
      { type: 'OR', x: 560, y: 210, label: '' },
      { type: 'OUTPUT', x: 560, y: 90, label: 'Sum' },
      { type: 'OUTPUT', x: 720, y: 210, label: 'Cout' }
    ],
    connections: [
      { fromGateIndex: 0, fromPort: 0, toGateIndex: 3, toPort: 0 },
      { fromGateIndex: 1, fromPort: 0, toGateIndex: 3, toPort: 1 },
      { fromGateIndex: 3, fromPort: 0, toGateIndex: 4, toPort: 0 },
      { fromGateIndex: 2, fromPort: 0, toGateIndex: 4, toPort: 1 },
      { fromGateIndex: 3, fromPort: 0, toGateIndex: 5, toPort: 0 },
      { fromGateIndex: 2, fromPort: 0, toGateIndex: 5, toPort: 1 },
      { fromGateIndex: 0, fromPort: 0, toGateIndex: 6, toPort: 0 },
      { fromGateIndex: 1, fromPort: 0, toGateIndex: 6, toPort: 1 },
      { fromGateIndex: 5, fromPort: 0, toGateIndex: 7, toPort: 0 },
      { fromGateIndex: 6, fromPort: 0, toGateIndex: 7, toPort: 1 },
      { fromGateIndex: 4, fromPort: 0, toGateIndex: 8, toPort: 0 },
      { fromGateIndex: 7, fromPort: 0, toGateIndex: 9, toPort: 0 }
    ]
  },
  {
    id: 'rs-latch',
    name: 'RS锁存器',
    description: 'RS Latch (NOR)',
    icon: 'R/S',
    gates: [
      { type: 'INPUT', x: 50, y: 100, label: 'R' },
      { type: 'INPUT', x: 50, y: 240, label: 'S' },
      { type: 'NOR', x: 230, y: 120, label: '' },
      { type: 'NOR', x: 230, y: 260, label: '' },
      { type: 'OUTPUT', x: 420, y: 120, label: 'Q' },
      { type: 'OUTPUT', x: 420, y: 260, label: "Q'" }
    ],
    connections: [
      { fromGateIndex: 0, fromPort: 0, toGateIndex: 2, toPort: 0 },
      { fromGateIndex: 1, fromPort: 0, toGateIndex: 3, toPort: 1 },
      { fromGateIndex: 2, fromPort: 0, toGateIndex: 3, toPort: 0 },
      { fromGateIndex: 3, fromPort: 0, toGateIndex: 2, toPort: 1 },
      { fromGateIndex: 2, fromPort: 0, toGateIndex: 4, toPort: 0 },
      { fromGateIndex: 3, fromPort: 0, toGateIndex: 5, toPort: 0 }
    ]
  },
  {
    id: 'd-flipflop',
    name: 'D触发器',
    description: 'D Flip-Flop',
    icon: 'D',
    gates: [
      { type: 'INPUT', x: 50, y: 80, label: 'D' },
      { type: 'INPUT', x: 50, y: 260, label: 'CLK' },
      { type: 'NOT', x: 200, y: 80, label: '' },
      { type: 'AND', x: 350, y: 80, label: '' },
      { type: 'AND', x: 350, y: 260, label: '' },
      { type: 'NOR', x: 520, y: 140, label: '' },
      { type: 'NOR', x: 520, y: 280, label: '' },
      { type: 'OUTPUT', x: 700, y: 140, label: 'Q' },
      { type: 'OUTPUT', x: 700, y: 280, label: "Q'" }
    ],
    connections: [
      { fromGateIndex: 0, fromPort: 0, toGateIndex: 2, toPort: 0 },
      { fromGateIndex: 2, fromPort: 0, toGateIndex: 3, toPort: 0 },
      { fromGateIndex: 1, fromPort: 0, toGateIndex: 3, toPort: 1 },
      { fromGateIndex: 0, fromPort: 0, toGateIndex: 4, toPort: 0 },
      { fromGateIndex: 1, fromPort: 0, toGateIndex: 4, toPort: 1 },
      { fromGateIndex: 3, fromPort: 0, toGateIndex: 5, toPort: 0 },
      { fromGateIndex: 4, fromPort: 0, toGateIndex: 6, toPort: 1 },
      { fromGateIndex: 5, fromPort: 0, toGateIndex: 6, toPort: 0 },
      { fromGateIndex: 6, fromPort: 0, toGateIndex: 5, toPort: 1 },
      { fromGateIndex: 5, fromPort: 0, toGateIndex: 7, toPort: 0 },
      { fromGateIndex: 6, fromPort: 0, toGateIndex: 8, toPort: 0 }
    ]
  }
]
