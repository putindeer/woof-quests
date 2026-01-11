# Woof Quests

Sistema de misiones por días para Minecraft (Paper), enfocado en progresión hardcore y aumento gradual de dificultad.

![License](https://img.shields.io/github/license/putindeer/woof-quests?style=flat-square)
![GitHub last commit](https://img.shields.io/github/last-commit/putindeer/woof-quests?style=flat-square)

---

## Misiones

### Día 1
- Hacer una espada de diamante  
- Hacer un hacha de diamante  
- Hacer un pico de diamante  
- Hacer una pala de diamante  
- Hacer una lanza de diamante  

### Día 2
- Matar **20 Wither Skeletons**
- Matar **20 Blazes**
- Matar **20 Piglins**
- Matar **5 Piglin Brutes**

### Día 3
- Entrar al End

### Día 4
- Matar **2 Evokers**
- Matar **15 Vindicators**
- Matar **2 Ravagers**

Cambio de dificultad:
- Resistencia e invisibilidad deshabilitada

### Día 5
- Abrir **5 Vaults**
- Abrir **3 Ominous Vaults**

### Día 6
- Matar **2 Withers**
- Matar **10 Guardians**
- Matar **3 Elder Guardians**

Cambio de dificultad:
- Los Withers pueden romper bloques

---

## Características generales

- Armadura deshabilitada  
  - Está permitido **todo lo que no sea armor**
- Al morir:
  - El jugador dropea su cabeza
  - Se reproduce un sonido
- Totems desactivados
- Progreso visible en las misiones (ej: `3/20`)
- Si no se completan las misiones del día → efecto **Wither**
- Anti combat-log:
  - Al desconectarse, el jugador queda clonado durante 30 segundos
  - Si ese player muere, el jugador también muere

---

## Build

Para compilar el plugin:
```bash
mvn clean package
```
