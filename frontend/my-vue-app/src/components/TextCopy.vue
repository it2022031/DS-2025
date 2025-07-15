
<!--<template>-->
<!--  <div class="p-6">-->
<!--    <h2 class="text-xl font-bold mb-4">Fetch Users from Backend</h2>-->

<!--    &lt;!&ndash; Button to trigger fetch &ndash;&gt;-->
<!--    <button @click="fetchUsers" class="bg-blue-500 text-white px-4 py-2 rounded mb-4">-->
<!--      Load Users-->
<!--    </button>-->

<!--    &lt;!&ndash; List of usernames &ndash;&gt;-->
<!--    <ul>-->
<!--      <li v-for="user in users" :key="user.id" class="mb-1">-->
<!--        {{ user.name }}-->
<!--      </li>-->
<!--    </ul>-->
<!--  </div>-->
<!--</template>-->

<!--<script setup>-->
<!--import { ref } from 'vue'-->

<!--const users = ref([])-->

<!--const fetchUsers = async () => {-->
<!--  try {-->
<!--    const response = await fetch('http://localhost:8080/api/users')-->
<!--    if (!response.ok) throw new Error('Network response was not ok')-->
<!--    const data = await response.json()-->
<!--    users.value = data-->
<!--  } catch (error) {-->
<!--    console.error('Error fetching users:', error)-->
<!--  }-->
<!--}-->
<!--</script>-->

<template>
  <div class="p-6">
    <h2 class="text-xl font-bold mb-4">Fetch & Send Data</h2>

    <!-- Input field and submit button -->
    <input
        v-model="inputText"
        type="text"
        placeholder="Enter name"
        class="border p-2 rounded mr-2"
    />
    <button @click="submitData" class="bg-green-600 text-white px-4 py-2 rounded mb-4">
      Submit Name
    </button>

    <!-- Button to trigger GET fetch -->
    <button @click="fetchUsers" class="bg-blue-500 text-white px-4 py-2 rounded mb-4 ml-2">
      Load Users
    </button>

    <!-- Displaying results -->
    <ul class="mt-4">
      <li v-for="user in users" :key="user.id" class="mb-1">
        {{ user.name }}
      </li>
    </ul>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const users = ref([])
const inputText = ref('')

const submitData = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/users', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ name: inputText.value }),
    })
    if (!response.ok) throw new Error('POST failed')

    // Optional: immediately fetch updated users
    await fetchUsers()
    inputText.value = ''
  } catch (error) {
    console.error('Error submitting data:', error)
  }
}

const fetchUsers = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/users')
    if (!response.ok) throw new Error('GET failed')
    const data = await response.json()
    users.value = data
  } catch (error) {
    console.error('Error fetching users:', error)
  }
}
</script>
