const CELL_EMPTY = 0;
const CELL_WALL = 1;
const CELL_INLET = 2;
const CELL_OUTLET = 3;
const CELL_AC = 4;
const CELL_VENTILATOR = 5;
const CELL_PURIFIER = 6;
const CELL_STERILIZER = 7;

class LBMSimulator {
    constructor(width, height) {
        this.width = width;
        this.height = height;
        this.size = width * height;
        
        this.D2Q9_WEIGHTS = [4/9, 1/9, 1/9, 1/9, 1/9, 1/36, 1/36, 1/36, 1/36];
        this.D2Q9_EX = [0, 1, 0, -1, 0, 1, -1, -1, 1];
        this.D2Q9_EY = [0, 0, 1, 0, -1, 1, 1, -1, -1];
        
        this.omega = 1.0;
        this.tau = 0.6;
        
        this.f = new Array(9).fill(null).map(() => new Float32Array(this.size));
        this.fNew = new Array(9).fill(null).map(() => new Float32Array(this.size));
        
        this.density = new Float32Array(this.size);
        this.ux = new Float32Array(this.size);
        this.uy = new Float32Array(this.size);
        
        this.obstacle = new Uint8Array(this.size);
        
        this.concentration = new Float32Array(this.size).fill(1.0);
        this.sterilizationMap = new Float32Array(this.size).fill(0);
        
        this.initialize();
    }
    
    initialize() {
        this.omega = 1.0 / this.tau;
        
        for (let i = 0; i < this.size; i++) {
            this.density[i] = 1.0;
            this.ux[i] = 0.0;
            this.uy[i] = 0.0;
            this.obstacle[i] = 0;
            
            for (let k = 0; k < 9; k++) {
                this.f[k][i] = this.D2Q9_WEIGHTS[k];
                this.fNew[k][i] = this.D2Q9_WEIGHTS[k];
            }
        }
    }
    
    setObstacle(x, y, isObstacle) {
        if (x >= 0 && x < this.width && y >= 0 && y < this.height) {
            const idx = y * this.width + x;
            this.obstacle[idx] = isObstacle ? CELL_WALL : CELL_EMPTY;
        }
    }
    
    setInlet(x, y, velocityX, velocityY) {
        if (x >= 0 && x < this.width && y >= 0 && y < this.height) {
            const idx = y * this.width + x;
            this.ux[idx] = velocityX;
            this.uy[idx] = velocityY;
            this.obstacle[idx] = CELL_INLET;
        }
    }
    
    setOutlet(x, y) {
        if (x >= 0 && x < this.width && y >= 0 && y < this.height) {
            const idx = y * this.width + x;
            this.obstacle[idx] = CELL_OUTLET;
        }
    }
    
    setACUnit(x, y, cmh, spreadRadius) {
        const velocity = cmh / (3600 * spreadRadius * 0.01);
        const dirs = [[1,0],[-1,0],[0,1],[0,-1]];
        
        for (const [dx, dy] of dirs) {
            for (let r = 0; r < spreadRadius; r++) {
                const nx = x + dx * r;
                const ny = y + dy * r;
                if (nx >= 0 && nx < this.width && ny >= 0 && ny < this.height) {
                    const idx = ny * this.width + nx;
                    if (this.obstacle[idx] === CELL_EMPTY) {
                        this.obstacle[idx] = CELL_AC;
                        this.ux[idx] = dx * velocity * 0.25;
                        this.uy[idx] = dy * velocity * 0.25;
                    }
                }
            }
        }
    }
    
    setSterilizer(x, y, cmh, radius) {
        const velocity = cmh / (3600 * Math.PI * radius * radius * 0.01);
        
        for (let dy = -radius; dy <= radius; dy++) {
            for (let dx = -radius; dx <= radius; dx++) {
                const dist = Math.sqrt(dx*dx + dy*dy);
                if (dist <= radius) {
                    const nx = x + dx;
                    const ny = y + dy;
                    if (nx >= 0 && nx < this.width && ny >= 0 && ny < this.height) {
                        const idx = ny * this.width + nx;
                        
                        this.sterilizationMap[idx] = Math.max(
                            this.sterilizationMap[idx],
                            1.0 - (dist / radius)
                        );
                        
                        if (dist > 0 && dist < radius * 0.5) {
                            this.obstacle[idx] = CELL_STERILIZER;
                            this.ux[idx] = (dx / dist) * velocity;
                            this.uy[idx] = (dy / dist) * velocity;
                        }
                    }
                }
            }
        }
    }
    
    setVentilator(x, y, cmh, dirX, dirY) {
        const velocity = cmh / 3600;
        const length = Math.sqrt(dirX*dirX + dirY*dirY);
        if (length > 0) {
            dirX /= length;
            dirY /= length;
        }
        
        for (let r = 0; r < 5; r++) {
            const nx = x + Math.round(dirX * r);
            const ny = y + Math.round(dirY * r);
            if (nx >= 0 && nx < this.width && ny >= 0 && ny < this.height) {
                const idx = ny * this.width + nx;
                if (this.obstacle[idx] === CELL_EMPTY) {
                    this.obstacle[idx] = CELL_VENTILATOR;
                    this.ux[idx] = dirX * velocity;
                    this.uy[idx] = dirY * velocity;
                }
            }
        }
    }
    
    setPurifier(x, y, cmh, radius) {
        const velocity = cmh / (3600 * Math.PI * radius * radius * 0.01);
        
        for (let dy = -radius; dy <= radius; dy++) {
            for (let dx = -radius; dx <= radius; dx++) {
                const dist = Math.sqrt(dx*dx + dy*dy);
                if (dist <= radius && dist > 0) {
                    const nx = x + dx;
                    const ny = y + dy;
                    if (nx >= 0 && nx < this.width && ny >= 0 && ny < this.height) {
                        const idx = ny * this.width + nx;
                        if (this.obstacle[idx] === CELL_EMPTY) {
                            this.obstacle[idx] = CELL_PURIFIER;
                            const angle = Math.atan2(dy, dx);
                            this.ux[idx] = Math.cos(angle + Math.PI/2) * velocity * 0.3;
                            this.uy[idx] = Math.sin(angle + Math.PI/2) * velocity * 0.3;
                        }
                    }
                }
            }
        }
    }
    
    computeEquilibrium(rho, ux, uy, k) {
        const ex = this.D2Q9_EX[k];
        const ey = this.D2Q9_EY[k];
        const w = this.D2Q9_WEIGHTS[k];
        
        const eu = ex * ux + ey * uy;
        const uSq = ux * ux + uy * uy;
        
        return w * rho * (1.0 + 3.0 * eu + 4.5 * eu * eu - 1.5 * uSq);
    }
    
    collision() {
        for (let i = 0; i < this.size; i++) {
            if (this.obstacle[i] === CELL_WALL) continue;
            
            let rho = 0.0;
            for (let k = 0; k < 9; k++) {
                rho += this.f[k][i];
            }
            
            let ux = 0.0;
            let uy = 0.0;
            for (let k = 0; k < 9; k++) {
                ux += this.D2Q9_EX[k] * this.f[k][i];
                uy += this.D2Q9_EY[k] * this.f[k][i];
            }
            ux /= rho;
            uy /= rho;
            
            if (this.obstacle[i] === CELL_INLET || 
                this.obstacle[i] === CELL_AC ||
                this.obstacle[i] === CELL_STERILIZER ||
                this.obstacle[i] === CELL_VENTILATOR ||
                this.obstacle[i] === CELL_PURIFIER) {
                ux = this.ux[i];
                uy = this.uy[i];
            }
            
            this.density[i] = rho;
            this.ux[i] = ux;
            this.uy[i] = uy;
            
            for (let k = 0; k < 9; k++) {
                const feq = this.computeEquilibrium(rho, ux, uy, k);
                this.f[k][i] = this.f[k][i] * (1.0 - this.omega) + feq * this.omega;
            }
        }
    }
    
    streaming() {
        for (let k = 0; k < 9; k++) {
            for (let y = 0; y < this.height; y++) {
                for (let x = 0; x < this.width; x++) {
                    const i = y * this.width + x;
                    
                    let nx = x - this.D2Q9_EX[k];
                    let ny = y - this.D2Q9_EY[k];
                    
                    if (nx < 0) nx = this.width - 1;
                    if (nx >= this.width) nx = 0;
                    if (ny < 0) ny = this.height - 1;
                    if (ny >= this.height) ny = 0;
                    
                    const ni = ny * this.width + nx;
                    
                    this.fNew[k][i] = this.f[k][ni];
                }
            }
        }
        
        [this.f, this.fNew] = [this.fNew, this.f];
    }
    
    bounceBack() {
        const opposite = [0, 3, 4, 1, 2, 7, 8, 5, 6];
        
        for (let i = 0; i < this.size; i++) {
            if (this.obstacle[i] === CELL_WALL) {
                for (let k = 1; k < 9; k++) {
                    const kOpp = opposite[k];
                    [this.f[k][i], this.f[kOpp][i]] = [this.f[kOpp][i], this.f[k][i]];
                }
            }
        }
    }
    
    stepConcentration() {
        const newConc = new Float32Array(this.size);
        
        for (let y = 0; y < this.height; y++) {
            for (let x = 0; x < this.width; x++) {
                const idx = y * this.width + x;
                if (this.obstacle[idx] === CELL_WALL) continue;
                
                let srcX = x - this.ux[idx] * 10;
                let srcY = y - this.uy[idx] * 10;
                
                srcX = Math.max(0, Math.min(this.width - 1, srcX));
                srcY = Math.max(0, Math.min(this.height - 1, srcY));
                
                const x0 = Math.floor(srcX);
                const x1 = Math.min(x0 + 1, this.width - 1);
                const y0 = Math.floor(srcY);
                const y1 = Math.min(y0 + 1, this.height - 1);
                const fx = srcX - x0;
                const fy = srcY - y0;
                
                const c00 = this.concentration[y0 * this.width + x0];
                const c10 = this.concentration[y0 * this.width + x1];
                const c01 = this.concentration[y1 * this.width + x0];
                const c11 = this.concentration[y1 * this.width + x1];
                
                let c = c00*(1-fx)*(1-fy) + c10*fx*(1-fy) + c01*(1-fx)*fy + c11*fx*fy;
                
                c *= 0.999;
                
                c *= (1.0 - this.sterilizationMap[idx] * 0.05);
                
                if (this.obstacle[idx] === CELL_INLET) c = 0.0;
                if (this.obstacle[idx] === CELL_OUTLET) c *= 0.9;
                
                newConc[idx] = Math.max(0, Math.min(1.0, c));
            }
        }
        
        this.concentration = newConc;
    }
    
    getDeadZones(threshold = 0.005) {
        const deadZone = new Uint8Array(this.size);
        let deadCount = 0;
        let totalFree = 0;
        
        for (let i = 0; i < this.size; i++) {
            if (this.obstacle[i] === CELL_WALL) continue;
            totalFree++;
            const vel = Math.sqrt(this.ux[i]*this.ux[i] + this.uy[i]*this.uy[i]);
            if (vel < threshold) {
                deadZone[i] = 1;
                deadCount++;
            }
        }
        
        return {
            map: Array.from(deadZone),
            percentage: totalFree > 0 ? (deadCount / totalFree * 100) : 0,
            count: deadCount
        };
    }
    
    step() {
        this.collision();
        this.streaming();
        this.bounceBack();
        this.stepConcentration();
    }
    
    simulate(steps) {
        for (let i = 0; i < steps; i++) {
            this.step();
        }
    }
    
    getVelocityMagnitude(x, y) {
        const idx = y * this.width + x;
        const ux = this.ux[idx];
        const uy = this.uy[idx];
        return Math.sqrt(ux * ux + uy * uy);
    }
    
    getVelocityField() {
        return {
            ux: Array.from(this.ux),
            uy: Array.from(this.uy),
            density: Array.from(this.density)
        };
    }
    
    getMaxVelocity() {
        let maxVel = 0.0;
        for (let i = 0; i < this.size; i++) {
            const vel = Math.sqrt(this.ux[i] * this.ux[i] + this.uy[i] * this.uy[i]);
            maxVel = Math.max(maxVel, vel);
        }
        return maxVel;
    }
    
    getAverageVelocity() {
        let sumVel = 0.0;
        let count = 0;
        for (let i = 0; i < this.size; i++) {
            if (this.obstacle[i] !== CELL_WALL) {
                const vel = Math.sqrt(this.ux[i] * this.ux[i] + this.uy[i] * this.uy[i]);
                sumVel += vel;
                count++;
            }
        }
        return count > 0 ? sumVel / count : 0.0;
    }
    
    getAverageConcentration() {
        let sum = 0;
        let count = 0;
        for (let i = 0; i < this.size; i++) {
            if (this.obstacle[i] !== CELL_WALL) {
                sum += this.concentration[i];
                count++;
            }
        }
        return count > 0 ? sum / count : 0;
    }
    
    getResults() {
        const deadZones = this.getDeadZones();
        return {
            velocityField: this.getVelocityField(),
            avgVelocity: this.getAverageVelocity(),
            maxVelocity: this.getMaxVelocity(),
            gridWidth: this.width,
            gridHeight: this.height,
            deadZones: deadZones,
            deadZonePercentage: deadZones.percentage,
            concentration: Array.from(this.concentration),
            avgConcentration: this.getAverageConcentration()
        };
    }
}

if (typeof module !== 'undefined' && module.exports) {
    module.exports = LBMSimulator;
}
