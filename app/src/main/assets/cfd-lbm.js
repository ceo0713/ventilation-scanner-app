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
            this.obstacle[idx] = isObstacle ? 1 : 0;
        }
    }
    
    setInlet(x, y, velocityX, velocityY) {
        if (x >= 0 && x < this.width && y >= 0 && y < this.height) {
            const idx = y * this.width + x;
            this.ux[idx] = velocityX;
            this.uy[idx] = velocityY;
            this.obstacle[idx] = 2;
        }
    }
    
    setOutlet(x, y) {
        if (x >= 0 && x < this.width && y >= 0 && y < this.height) {
            const idx = y * this.width + x;
            this.obstacle[idx] = 3;
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
            if (this.obstacle[i] === 1) continue;
            
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
            
            if (this.obstacle[i] === 2) {
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
            if (this.obstacle[i] === 1) {
                for (let k = 1; k < 9; k++) {
                    const kOpp = opposite[k];
                    [this.f[k][i], this.f[kOpp][i]] = [this.f[kOpp][i], this.f[k][i]];
                }
            }
        }
    }
    
    step() {
        this.collision();
        this.streaming();
        this.bounceBack();
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
            if (this.obstacle[i] !== 1) {
                const vel = Math.sqrt(this.ux[i] * this.ux[i] + this.uy[i] * this.uy[i]);
                sumVel += vel;
                count++;
            }
        }
        return count > 0 ? sumVel / count : 0.0;
    }
}

if (typeof module !== 'undefined' && module.exports) {
    module.exports = LBMSimulator;
}
