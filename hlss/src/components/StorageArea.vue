<template>
  <div class="storage-area" :class="{ active: active }" @click="$emit('click')">
    <h3>{{ area.name }}</h3>
    <p>{{ area.description }}</p>
  </div>
</template>

<script>
export default {
  name: 'StorageArea',
  props: {
    area: {
      type: Object,
      required: true
    },
    active: {
      type: Boolean,
      default: false
    }
  }
}
</script>