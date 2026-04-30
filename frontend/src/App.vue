<template>
  <component :is="layout">
    <router-view />
  </component>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AdminLayout from './layouts/AdminLayout.vue'
import MobileLayout from './layouts/MobileLayout.vue'
import FullScreenLayout from './layouts/FullScreenLayout.vue'

const route = useRoute()
const layout = computed(() => {
  switch (route.meta.layout) {
    case 'admin':
      return AdminLayout
    case 'mobile':
      return MobileLayout
    case 'fullscreen':
      return FullScreenLayout
    default:
      return AdminLayout
  }
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
#app {
  width: 100%;
  height: 100%;
}
</style>
