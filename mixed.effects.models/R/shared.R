# ------------------------------------------------------------------------------

get_data_from_file <- function (file_name) {
  # Load Data
  dataNotClean <- read.csv(file_name)
  data <- na.omit(dataNotClean)

  data$t_norm <- data$t / 365
  data$base_slope <- data$t / 365
  data$intervention_slope <- data$t_post / 365
  data$intervention_jump <- data$intervention

  data$t_pre <- NULL
  data$t_post <- NULL
  data$intervention <- NULL

  return(data)
}

# ------------------------------------------------------------------------------

# ------------------------------------------------------------------------------

get_data_from_file_for_linear_regression <- function (file_name) {
  # Load Data
  dataNotClean <- read.csv(file_name)
  data <- na.omit(dataNotClean)

  data$slope <- data$t / 365
  data$intervention_jump <- data$intervention

  data <- data[data$intervention == 1, ]
  data$t_pre <- NULL
  data$t_post <- NULL
  data$intervention <- NULL

  return(data)
}

# ------------------------------------------------------------------------------

# ------------------------------------------------------------------------------

save_png <- function(expression, file, width = 15, height = 10) {
  png(file, width = width, height = height, units = "cm", res = 300, pointsize = 8)
  on.exit(dev.off())
  force(expression)
}

# ------------------------------------------------------------------------------

save_multipanel_png <- function(expression, file, mfrow = c(2, 2), width = 30, height = 20) {
  png(file, width = width, height = height, units = "cm", res = 300, pointsize = 8)
  par(mfrow = mfrow)
  force(expression)
  par(mfrow = c(1, 1))
  dev.off()
}

# ------------------------------------------------------------------------------
