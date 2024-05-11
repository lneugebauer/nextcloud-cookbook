<script setup lang="ts">
import {defineProps} from 'vue'
import type {NCBadge} from "./types";
import NCBadgeComponent from "./NCBadge.vue"

interface Props {
  badges: NCBadge[]
}

const props = defineProps<Props>()
</script>

<template>
  <div class="container">
    <NCBadgeComponent v-for="badge in badges" :key="badge.link" :badge="badge" size="medium"/>
  </div>
</template>

<style scoped>
.container {
  display: flex;
  flex-wrap: wrap;
}
</style>