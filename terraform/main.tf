resource "docker_image" "selenium" {
  name         = var.selenium_image
  keep_locally = true
}

resource "docker_container" "selenium" {
  name     = var.container_name
  image    = docker_image.selenium.image_id
  shm_size = 2 * 1024 * 1024 * 1024
  must_run = true
  restart  = "no"
  env = [
    "SE_NODE_MAX_SESSIONS=4",
    "SE_NODE_OVERRIDE_MAX_SESSIONS=true",
  ]

  ports {
    internal = 4444
    external = var.host_port
  }
}
