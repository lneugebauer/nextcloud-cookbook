<script setup lang="ts">
import {defineProps} from 'vue'
import type {NCBadge} from "./types.js";

interface Props {
  badge: NCBadge
  size: 'small' | 'medium'
}

const props = defineProps<Props>()
</script>

<template>
  <a :href="badge.link">
    <img class="badge" :class="[size]" :src="badge.src" :alt="badge.alt"/>
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
  height: 30px;
}
</style>