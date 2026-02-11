# ------------------------------------------------------------------------------
library(data.table)

get_data <- function(language, group, transition_n_days) {
  files <- list.files(file.path("data", language, "time_series", group),
                      pattern = "\\.csv$",
                      full.names = TRUE,
                      recursive = TRUE)

  process_file <- function(file) {
    dt <- fread(file)
    dt <- na.omit(dt)

    # create new columns
    dt$t_norm <- dt$t_day / 365
    dt$base_slope <- dt$t_day / 365
    dt$intervention_slope <- dt$t_day / 365 * dt$intervention
    dt$intervention_jump <- dt$intervention

    # some filtering
    dt$intervention_jump[dt$t_day == 0] <- 0
    dt[, c("intervention") := NULL]
    dt <- dt[dt$relative_introduction_time >= 0, ]
    dt <- dt[dt$t_day <= 0 | t_day >= transition_n_days]

    # add some columns for potential interactions
    dt$time_saturated <- 1 - exp(-1 * dt$relative_introduction_time)
    dt$log_time <- log(dt$relative_introduction_time + 1)

    return(dt)
  }

  data_list <- lapply(files, process_file)

  finalData <- rbindlist(data_list)
  setDF(finalData)

  return(finalData)
}

# ------------------------------------------------------------------------------

# ------------------------------------------------------------------------------

save_png <- function(expression, file, width = 15, height = 10) {
  png(file, width = width, height = height, units = "cm", res = 300, pointsize = 8)
  on.exit(dev.off())
  force(expression)
}

# ------------------------------------------------------------------------------

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
