output "selenium_remote_url" {
  description = "Remote WebDriver endpoint exposed by the Selenium container."
  value       = "http://localhost:${docker_container.selenium.ports[0].external}"
}

output "selenium_container_name" {
  description = "Name of the provisioned Selenium container."
  value       = docker_container.selenium.name
}
