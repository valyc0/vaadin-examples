import { LitElement, html, css } from 'lit';
import { customElement, property } from 'lit/decorators.js';

/**
 * Graph component using plain JavaScript for visualization
 * Shows products as nodes and creates edges based on shared categories
 */
@customElement('graph-component')
export class GraphComponent extends LitElement {
    @property({ type: Array })
    graphData: any[] = [];

    private svg: SVGSVGElement | null = null;
    private nodes: any[] = [];
    private links: any[] = [];
    private simulation: any = null;

    static styles = css`
        :host {
            display: block;
            width: 100%;
            height: 100%;
            position: relative;
        }

        #graph-container {
            width: 100%;
            height: 600px;
            border: 1px solid #ddd;
            border-radius: 4px;
            background: #fafafa;
            position: relative;
            overflow: hidden;
        }

        svg {
            width: 100%;
            height: 100%;
        }

        .node {
            cursor: pointer;
            transition: all 0.3s ease;
        }

        .node:hover {
            opacity: 0.8;
        }

        .node-circle {
            fill: #1976d2;
            stroke: #fff;
            stroke-width: 2px;
        }

        .node-text {
            fill: #333;
            font-size: 12px;
            font-weight: 500;
            pointer-events: none;
            text-anchor: middle;
        }

        .link {
            stroke: #999;
            stroke-opacity: 0.6;
            stroke-width: 1.5px;
        }

        .category-electronics { fill: #2196F3; }
        .category-audio { fill: #FF9800; }
        .category-accessories { fill: #4CAF50; }
        .category-components { fill: #9C27B0; }
        .category-network { fill: #F44336; }
        .category-peripherals { fill: #00BCD4; }
        .category-furniture { fill: #795548; }
        .category-smart-home { fill: #8BC34A; }
        .category-storage { fill: #607D8B; }
        .category-default { fill: #1976d2; }

        .legend {
            position: absolute;
            top: 10px;
            right: 10px;
            background: white;
            padding: 10px;
            border-radius: 4px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            font-size: 12px;
        }

        .legend-item {
            display: flex;
            align-items: center;
            margin-bottom: 5px;
        }

        .legend-color {
            width: 12px;
            height: 12px;
            border-radius: 50%;
            margin-right: 8px;
        }
    `;

    render() {
        return html`
            <div id="graph-container">
                <svg id="graph-svg"></svg>
                <div class="legend">
                    <div style="font-weight: bold; margin-bottom: 8px;">Categorie</div>
                    <div class="legend-item">
                        <div class="legend-color category-electronics"></div>
                        <span>Elettronica</span>
                    </div>
                    <div class="legend-item">
                        <div class="legend-color category-audio"></div>
                        <span>Audio</span>
                    </div>
                    <div class="legend-item">
                        <div class="legend-color category-accessories"></div>
                        <span>Accessori</span>
                    </div>
                    <div class="legend-item">
                        <div class="legend-color category-components"></div>
                        <span>Componenti</span>
                    </div>
                    <div class="legend-item">
                        <div class="legend-color category-network"></div>
                        <span>Rete</span>
                    </div>
                    <div class="legend-item">
                        <div class="legend-color category-peripherals"></div>
                        <span>Periferiche</span>
                    </div>
                    <div class="legend-item">
                        <div class="legend-color category-storage"></div>
                        <span>Storage</span>
                    </div>
                </div>
            </div>
        `;
    }

    updated(changedProperties: Map<string, any>) {
        if (changedProperties.has('graphData') && this.graphData.length > 0) {
            this.initGraph();
        }
    }

    firstUpdated() {
        const svgElement = this.shadowRoot?.getElementById('graph-svg');
        if (svgElement) {
            this.svg = svgElement as unknown as SVGSVGElement;
        }
        if (this.graphData.length > 0) {
            this.initGraph();
        }
    }

    private getCategoryClass(category: string): string {
        const categoryMap: { [key: string]: string } = {
            'Elettronica': 'category-electronics',
            'Audio': 'category-audio',
            'Accessori': 'category-accessories',
            'Componenti': 'category-components',
            'Rete': 'category-network',
            'Periferiche': 'category-peripherals',
            'Arredamento': 'category-furniture',
            'Smart Home': 'category-smart-home',
            'Storage': 'category-storage'
        };
        return categoryMap[category] || 'category-default';
    }

    private initGraph() {
        if (!this.svg) return;

        // Clear previous graph
        while (this.svg.firstChild) {
            this.svg.removeChild(this.svg.firstChild);
        }

        const width = this.svg.clientWidth || 800;
        const height = this.svg.clientHeight || 600;

        // Prepare nodes
        this.nodes = this.graphData.map((product: any) => ({
            id: product.id,
            name: product.name,
            category: product.category,
            price: product.price,
            quantity: product.quantity,
            x: Math.random() * width,
            y: Math.random() * height,
            vx: 0,
            vy: 0
        }));

        // Create links between nodes with same category
        this.links = [];
        const categoryGroups: { [key: string]: any[] } = {};
        
        this.nodes.forEach(node => {
            if (!categoryGroups[node.category]) {
                categoryGroups[node.category] = [];
            }
            categoryGroups[node.category].push(node);
        });

        // Create links within each category group
        Object.values(categoryGroups).forEach(group => {
            for (let i = 0; i < group.length; i++) {
                for (let j = i + 1; j < group.length && j < i + 3; j++) {
                    this.links.push({
                        source: group[i],
                        target: group[j]
                    });
                }
            }
        });

        // Create SVG groups
        const g = document.createElementNS('http://www.w3.org/2000/svg', 'g');
        this.svg.appendChild(g);

        // Draw links
        const linksGroup = document.createElementNS('http://www.w3.org/2000/svg', 'g');
        linksGroup.setAttribute('class', 'links');
        g.appendChild(linksGroup);

        this.links.forEach(link => {
            const line = document.createElementNS('http://www.w3.org/2000/svg', 'line');
            line.setAttribute('class', 'link');
            line.setAttribute('x1', String(link.source.x));
            line.setAttribute('y1', String(link.source.y));
            line.setAttribute('x2', String(link.target.x));
            line.setAttribute('y2', String(link.target.y));
            linksGroup.appendChild(line);
        });

        // Draw nodes
        const nodesGroup = document.createElementNS('http://www.w3.org/2000/svg', 'g');
        nodesGroup.setAttribute('class', 'nodes');
        g.appendChild(nodesGroup);

        this.nodes.forEach(node => {
            const nodeGroup = document.createElementNS('http://www.w3.org/2000/svg', 'g');
            nodeGroup.setAttribute('class', 'node');
            nodeGroup.setAttribute('transform', `translate(${node.x},${node.y})`);

            // Circle
            const circle = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
            circle.setAttribute('r', String(Math.sqrt(node.quantity) * 2 + 10));
            circle.setAttribute('class', `node-circle ${this.getCategoryClass(node.category)}`);
            
            // Title (tooltip)
            const title = document.createElementNS('http://www.w3.org/2000/svg', 'title');
            title.textContent = `${node.name}\nCategoria: ${node.category}\nPrezzo: €${node.price}\nQuantità: ${node.quantity}`;
            circle.appendChild(title);

            // Text label
            const text = document.createElementNS('http://www.w3.org/2000/svg', 'text');
            text.setAttribute('class', 'node-text');
            text.setAttribute('dy', String(Math.sqrt(node.quantity) * 2 + 25));
            text.textContent = node.name.length > 15 ? node.name.substring(0, 15) + '...' : node.name;

            nodeGroup.appendChild(circle);
            nodeGroup.appendChild(text);
            nodesGroup.appendChild(nodeGroup);

            // Add drag behavior
            let isDragging = false;
            let startX = 0, startY = 0;

            nodeGroup.addEventListener('mousedown', (e) => {
                isDragging = true;
                startX = e.clientX - node.x;
                startY = e.clientY - node.y;
                e.preventDefault();
            });

            document.addEventListener('mousemove', (e) => {
                if (isDragging) {
                    node.x = e.clientX - startX;
                    node.y = e.clientY - startY;
                    this.updatePositions();
                }
            });

            document.addEventListener('mouseup', () => {
                isDragging = false;
            });
        });

        // Simple force simulation
        this.startSimulation(width, height);
    }

    private startSimulation(width: number, height: number) {
        const iterations = 100;
        const alpha = 0.1;

        for (let i = 0; i < iterations; i++) {
            // Apply forces
            this.nodes.forEach(node => {
                // Center force
                node.vx += (width / 2 - node.x) * 0.001;
                node.vy += (height / 2 - node.y) * 0.001;

                // Collision force
                this.nodes.forEach(other => {
                    if (node !== other) {
                        const dx = other.x - node.x;
                        const dy = other.y - node.y;
                        const dist = Math.sqrt(dx * dx + dy * dy);
                        const minDist = 100;

                        if (dist < minDist && dist > 0) {
                            const force = (minDist - dist) / dist * 0.1;
                            node.vx -= dx * force;
                            node.vy -= dy * force;
                        }
                    }
                });

                // Link force
                this.links.forEach(link => {
                    if (link.source === node) {
                        const dx = link.target.x - node.x;
                        const dy = link.target.y - node.y;
                        const dist = Math.sqrt(dx * dx + dy * dy);
                        const targetDist = 150;
                        
                        if (dist > 0) {
                            const force = (dist - targetDist) / dist * 0.01;
                            node.vx += dx * force;
                            node.vy += dy * force;
                        }
                    }
                    if (link.target === node) {
                        const dx = link.source.x - node.x;
                        const dy = link.source.y - node.y;
                        const dist = Math.sqrt(dx * dx + dy * dy);
                        const targetDist = 150;
                        
                        if (dist > 0) {
                            const force = (dist - targetDist) / dist * 0.01;
                            node.vx += dx * force;
                            node.vy += dy * force;
                        }
                    }
                });

                // Apply velocity
                node.x += node.vx * alpha;
                node.y += node.vy * alpha;

                // Damping
                node.vx *= 0.9;
                node.vy *= 0.9;

                // Keep in bounds
                const margin = 50;
                node.x = Math.max(margin, Math.min(width - margin, node.x));
                node.y = Math.max(margin, Math.min(height - margin, node.y));
            });

            if (i % 10 === 0) {
                this.updatePositions();
            }
        }

        this.updatePositions();
    }

    private updatePositions() {
        if (!this.svg) return;

        const nodesGroup = this.svg.querySelector('.nodes');
        const linksGroup = this.svg.querySelector('.links');

        if (!nodesGroup || !linksGroup) return;

        // Update node positions
        const nodeElements = nodesGroup.querySelectorAll('.node');
        nodeElements.forEach((nodeElement, i) => {
            const node = this.nodes[i];
            nodeElement.setAttribute('transform', `translate(${node.x},${node.y})`);
        });

        // Update link positions
        const linkElements = linksGroup.querySelectorAll('.link');
        linkElements.forEach((linkElement, i) => {
            const link = this.links[i];
            linkElement.setAttribute('x1', String(link.source.x));
            linkElement.setAttribute('y1', String(link.source.y));
            linkElement.setAttribute('x2', String(link.target.x));
            linkElement.setAttribute('y2', String(link.target.y));
        });
    }
}

declare global {
    interface HTMLElementTagNameMap {
        'graph-component': GraphComponent;
    }
}
