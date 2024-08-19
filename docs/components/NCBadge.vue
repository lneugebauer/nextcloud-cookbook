<script setup lang="ts">
import type {NCBadge} from "./types.js";

interface Props {
  alt: string
  link: string
  size: 'small' | 'medium'
  src: string
}

const props = defineProps<Props>()
</script>

<template>
  <a :href="link">
    <img class="badge" :class="[size]" :src="src" :alt="alt"/>
  </a>
</template>

<style scoped>
.badge {
  width: auto;
}

.medium {
  height: 80px;
}

.small {
  height: 32px;
}
</style>