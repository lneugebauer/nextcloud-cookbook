<template>
  <div class="container">
    <slot />
  </div>
</template>

<style scoped>
.container {
  display: flex;
  flex-wrap: wrap;
  height: 30px;
  gap: 15px;
}
</style>