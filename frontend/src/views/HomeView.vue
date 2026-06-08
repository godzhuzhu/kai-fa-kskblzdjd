<template>
  <div class="home-view">
    <HomeBackground />
    <Home />
  </div>
</template>

<script setup lang="ts">
import HomeBackground from '../components/HomeBackground.vue'
import Home from '../components/Home.vue'
</script>

<style scoped>
.home-view {
  width: 100%;
  height: 100vh;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
</style>
