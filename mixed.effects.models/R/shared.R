# ------------------------------------------------------------------------------

get_data <- function(language, group, transition_n_days) {
  files <- list.files(file.path("data", language, "time_series", group),
                      pattern = "\\.csv$",
                      full.names = TRUE,
                      recursive = TRUE)

  process_file <- function(file) {
    dt <- fread(file)
    dt <- na.omit(dt)

    dt$cloneCoverage <- dt$cloneCoverage * 100

    # create new columns
    dt$t_norm <- dt$t_day / 365
    dt$base_slope <- dt$t_day / 365
    dt$slope_change <- dt$t_day / 365 * dt$intervention
    dt$level_change <- dt$intervention

    # some filtering
    dt$level_change[dt$t_day == 0] <- 0
    dt[, c("intervention") := NULL]
    dt <- dt[dt$relative_introduction_time >= 0, ]
    dt <- dt[dt$t_day <= 0 | t_day >= transition_n_days]

    # add some columns for potential interactions
    dt$time_saturated <- 1 - exp(-1 * dt$relative_introduction_time)
    dt$time_log <- log(dt$relative_introduction_time + 1)
    dt$time_e <- exp(dt$relative_introduction_time)
    dt$week <- dt$t_day / 7

    return(dt)
  }

  data_list <- lapply(files, process_file)

  finalData <- rbindlist(data_list)
  setDF(finalData)

  finalData <- finalData %>%
    distinct(week, id, .keep_all = TRUE)


  finalData <- finalData[complete.cases(finalData), ]
  return(finalData)
}

# ------------------------------------------------------------------------------

# ------------------------------------------------------------------------------

save_png <- function(expression, path, width = 15, height = 10) {
  png(path, width = width, height = height, units = "cm", res = 300, pointsize = 10)
  on.exit(dev.off())
  force(expression)
}

# ------------------------------------------------------------------------------

# ------------------------------------------------------------------------------

save_multipanel_png <- function(expression, path, mfrow = c(2, 2), width = 15, height = 10) {
  png(path, width = width, height = height, units = "cm", res = 300, pointsize = 10)
  par(mfrow = mfrow, oma = c(0, 0, 0, 0), mar = c(5, 5, 2, 1))
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


get_transition_time <- function(lang, metric_name) {
  model_stability_data <- data.frame(
    Metric = c(
      "longFiles", "longFilesDensity", "longMethods", "longMethodDensity",
      "deepNesting", "deepNestingDensity", "cloneCoverage", "averageFileLoc",
      "comprehensibilityDensity", "correctnessDensity", "documentationDensity",
      "efficiencyDensity", "errorHandlingDensity", "redundancyDensity",
      "securityDensity", "structureDensity", "findingsDensity",
      "numberOfForks", "numberOfIssues", "numberOfContributors"
    ),
    java =   c(46, 4, 46, 4, 46, 45, 4, 4, 20, 4, 20, 0, 4, 0, 0, 45, 20, 0, 0, 2),
    python = c(30, "x", 5, 0, "x", 33, 0, 0, 0, 0, 0, 0, "x", "x", 36, 28, 0, 0, 0, 0),
    cpp =    c("x", 0, "x", "x", "x", 0, 0, 51, 0, 0, 0, "x", "x", 40, 21, "x", 0, 0, 0, 0)
  )

  if (!lang %in% colnames(model_stability_data)) {
    stop(paste("Language", lang, "not found in table."))
  }

  row_idx <- which(model_stability_data$Metric == metric_name)

  if (length(row_idx) == 0) {
    stop(paste("Metric", metric_name, "not found in table."))
  }

  return(as.numeric(model_stability_data[row_idx, lang]))
}
