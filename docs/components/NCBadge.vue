<script setup lang="ts">
interface Props {
  alt: string
  eventName: string | null,
  link: string
  size: 'small' | 'medium'
  src: string
}

const props = defineProps<Props>()
</script>

<template>
  <a :href="link" :data-umami-event="eventName">
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