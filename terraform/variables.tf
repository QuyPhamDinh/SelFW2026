variable "container_name" {
  description = "Name assigned to the temporary Selenium container."
  type        = string
  default     = "selfw-selenium"
}

variable "host_port" {
  description = "Host port mapped to Selenium Grid."
  type        = number
  default     = 4444
}

variable "selenium_image" {
  description = "Pinned multi-architecture Selenium Chromium image."
  type        = string
  default     = "selenium/standalone-chromium:4.25.0-20241010"
}
