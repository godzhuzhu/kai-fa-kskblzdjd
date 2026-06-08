<template>
  <div class="background">
    <div class="stars"></div>
    <div class="twinkling"></div>
    <div class="clouds"></div>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}
.stars, .twinkling, .clouds {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}
.stars {
  background: #000 url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='100' height='100'%3E%3Ccircle cx='10' cy='10' r='1' fill='white' opacity='0.8'/%3E%3Ccircle cx='50' cy='30' r='1.5' fill='white' opacity='0.6'/%3E%3Ccircle cx='80' cy='60' r='1' fill='white' opacity='0.9'/%3E%3Ccircle cx='30' cy='80' r='1.2' fill='white' opacity='0.5'/%3E%3Ccircle cx='70' cy='15' r='0.8' fill='white' opacity='0.7'/%3E%3Ccircle cx='90' cy='85' r='1' fill='white' opacity='0.4'/%3E%3C/svg%3E") repeat;
  z-index: 0;
}
.twinkling {
  background: transparent url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='200' height='200'%3E%3Ccircle cx='20' cy='40' r='2' fill='white' opacity='0.3'/%3E%3Ccircle cx='120' cy='100' r='1' fill='white' opacity='0.5'/%3E%3Ccircle cx='160' cy='30' r='1.5' fill='white' opacity='0.2'/%3E%3Ccircle cx='60' cy='150' r='2' fill='white' opacity='0.4'/%3E%3C/svg%3E") repeat;
  z-index: 1;
  animation: twinkle 200s linear infinite;
}
.clouds {
  background: transparent url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='400' height='200'%3E%3Cpath d='M50,100 Q80,60 120,80 Q160,50 200,80 Q240,60 280,90 Q320,70 350,100 Z' fill='%23222' opacity='0.1'/%3E%3C/svg%3E") repeat;
  z-index: 2;
  animation: move 250s linear infinite;
  opacity: 0.4;
}
@keyframes twinkle {
  0% { transform: translate(0, 0); }
  100% { transform: translate(-200px, -200px); }
}
@keyframes move {
  0% { transform: translate(0, 0); }
  100% { transform: translate(-400px, 0); }
}
</style>
