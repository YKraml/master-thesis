# ------------------------------------------------------------------------------

get_data_from_file <- function (file_name) {
  dataNotClean <- read.csv(file_name)
  data <- na.omit(dataNotClean)

  data$t_norm <- data$t / 365
  data$base_slope <- data$t / 365
  data$intervention_slope <- data$t_post / 365
  data$intervention_jump <- data$intervention
  data$relative_introduction_time <- data$relative_introduction_time / 60 / 60 / 24 / 365 # in years now

  data$t_pre <- NULL
  data$t_post <- NULL
  data$intervention <- NULL

  data <- data[data$relative_introduction_time >= 0, ]

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

# ------------------------------------------------------------------------------

# Helper function to format p-values nicely
fmt_p <- function(p) {
  # Safety check: ensure p exists, is not NULL, and is numeric
  if (is.null(p) || length(p) == 0 || !is.numeric(p)) {
    return("Invalid P-Value")
  }

  # Handle NA values (common in correlation tests with constant data)
  if (is.na(p)) {
    return("NA")
  }

  if (p < 0.001) {
    return("< 0.001")
  }

  return(paste0("= ", format(round(p, 3), nsmall = 3)))
}

# ------------------------------------------------------------------------------
