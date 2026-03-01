<template>

  <div class="container-fluid vh-100">

    <div class="row h-100">

      <!-- LEFT SIDE -->
      <div class="col-md-6 d-none d-md-flex flex-column justify-content-center align-items-center bg-primary text-white">

        <img
            src=""
            class="mb-3"
            style="width:90px"
        />

        <h3 class="fw-bold">
          VANTIX HRM
        </h3>

        <p class="mb-4 text-center">
          Human Resource Management System
        </p>

        <img
            src=""
            class="img-fluid"
            style="max-height:400px"
        />

      </div>


      <!-- RIGHT SIDE -->
      <div class="col-md-6 d-flex justify-content-center align-items-center">
            <!-- Router View -->
            <router-view />
      </div>


    </div>

  </div>

</template>