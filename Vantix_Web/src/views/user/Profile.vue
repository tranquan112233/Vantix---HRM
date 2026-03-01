<template>
  <div class="container py-5">

    <h3 class="fw-bold text-center mb-4">
      👤 My Profile
    </h3>

    <div class="row g-4">

      <!-- LEFT PROFILE CARD -->
      <div class="col-lg-4">
        <div class="card profile-card text-center p-4">

          <!-- AVATAR UPLOAD -->
          <div class="avatar-wrapper mx-auto" @click="openFilePicker">

            <img
                :src="avatarUrl"
                @error="setDefaultAvatar"
                class="avatar-lg"
            />

            <div class="avatar-overlay">
              📷 Change
            </div>

          </div>

          <!-- hidden file input -->
          <input
              ref="fileInput"
              type="file"
              accept="image/*"
              class="d-none"
              @change="uploadAvatar"
          />

          <h5 class="mt-3 mb-1 fw-semibold">
            {{ profile.fullName }}
          </h5>

          <p class="text-muted mb-2">
            {{ profile.position }}
          </p>

          <span class="badge bg-primary mb-3">
            {{ profile.department }}
          </span>

          <hr />

          <div class="small text-muted">
            <div>{{ profile.email }}</div>
            <div>{{ profile.phone }}</div>
          </div>

        </div>
      </div>

      <!-- RIGHT EDIT FORM -->
      <div class="col-lg-8">
        <div class="card profile-form p-4">

          <h5 class="fw-bold mb-4">
            Edit Information
          </h5>

          <div v-if="loading" class="text-center py-5">
            <div class="spinner-border text-primary"></div>
            <p class="mt-2">Loading profile...</p>
          </div>

          <form v-else @submit.prevent="updateProfile">

            <div class="row g-3">

              <div class="col-md-6">
                <label class="form-label">Username</label>
                <input class="form-control" v-model="profile.username" disabled />
              </div>

              <div class="col-md-6">
                <label class="form-label">Email</label>
                <input class="form-control" v-model="profile.email" disabled />
              </div>

              <div class="col-md-6">
                <label class="form-label">Full Name</label>
                <input class="form-control" v-model="profile.fullName" required />
              </div>

              <div class="col-md-6">
                <label class="form-label">Phone</label>
                <input class="form-control" v-model="profile.phone" />
              </div>

              <div class="col-12">
                <label class="form-label">Address</label>
                <input class="form-control" v-model="profile.address" />
              </div>

              <div class="col-md-6">
                <label class="form-label">Birth Date</label>
                <input type="date" class="form-control" v-model="profile.birthDate" />
              </div>

              <div class="col-md-6">
                <label class="form-label">Gender</label>
                <select class="form-select" v-model="profile.gender">
                  <option value="MALE">Male</option>
                  <option value="FEMALE">Female</option>
                  <option value="OTHER">Other</option>
                </select>
              </div>

              <div class="col-md-6">
                <label class="form-label">Department</label>
                <input class="form-control" :value="profile.department" disabled />
              </div>

              <div class="col-md-6">
                <label class="form-label">Position</label>
                <input class="form-control" :value="profile.position" disabled />
              </div>

            </div>

            <div class="text-end mt-4">
              <button class="btn btn-primary px-4">
                💾 Update Profile
              </button>

              <span v-if="success" class="text-success ms-3 fw-semibold">
                ✔ Updated successfully!
              </span>
            </div>

          </form>

        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import profileservice from '@/services/profileservice.service.js'

const profile = ref({})
const loading = ref(true)
const success = ref(false)
const avatarUrl = ref('')
const fileInput = ref(null)

/* LOAD PROFILE */
const loadProfile = async () => {
  try {
    const res = await profileservice.getMyProfile()
    profile.value = res.data
    setTimeout(reloadAvatar, 0)
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

/* UPDATE PROFILE */
const updateProfile = async () => {
  success.value = false
  try {
    await profileservice.updateProfile(profile.value)
    success.value = true
  } catch {
    alert('Update failed')
  }
}

/* OPEN FILE PICKER */
const openFilePicker = () => {
  fileInput.value.click()
}

/* RELOAD AVATAR */
const reloadAvatar = () => {
  const id = profile.value.userId || profile.value.id
  if (!id) return

  avatarUrl.value =
      `http://localhost:8080/avatars/${id}.jpg?t=${Date.now()}`
}

/* DEFAULT AVATAR */
const setDefaultAvatar = () => {
  avatarUrl.value = '/default-avatar.png'
}

/* UPLOAD AVATAR */
const uploadAvatar = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  // preview instantly
  avatarUrl.value = URL.createObjectURL(file)

  try {
    await profileservice.uploadAvatar(file)
    reloadAvatar()
  } catch (e) {
    console.error(e)
    alert('Upload avatar failed')
  }
}

onMounted(loadProfile)
</script>

<style scoped>
.profile-card,
.profile-form{
  border:none;
  border-radius:16px;
  box-shadow:0 8px 25px rgba(0,0,0,0.08);
}

.avatar-lg{
  width:140px;
  height:140px;
  border-radius:50%;
  object-fit:cover;
  border:4px solid #f1f1f1;
}

/* avatar upload */
.avatar-wrapper{
  position:relative;
  width:140px;
  cursor:pointer;
}

.avatar-overlay{
  position:absolute;
  inset:0;
  border-radius:50%;
  background:rgba(0,0,0,0.5);
  color:white;
  display:flex;
  align-items:center;
  justify-content:center;
  font-size:14px;
  opacity:0;
  transition:0.3s;
}

.avatar-wrapper:hover .avatar-overlay{
  opacity:1;
}

.card{
  transition:0.25s;
}

.card:hover{
  transform:translateY(-4px);
}
</style>