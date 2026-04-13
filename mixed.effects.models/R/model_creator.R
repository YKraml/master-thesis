get_model <- function(data, language_name, group, metric_name, interaction) {

  # 1. Setup paths and formulas
  out_dir <- file.path("plots", language_name, "interrupted_time_series", group, metric_name)
  prefix <- sprintf("%s.%s.%s.", language_name, group, metric_name)

  dir.create(out_dir, recursive = TRUE, showWarnings = FALSE)

  fixed_form <- as.formula(sprintf("%s ~ %s * (1 + base_slope + slope_change + level_change)", metric_name, interaction))
  random_form <- ~ 1 + base_slope + slope_change + level_change | id

  # ---------------------------------------------------------
  # ATTEMPT 1: Complex Model
  # ---------------------------------------------------------
  model_complex_file <- file.path(out_dir, paste0(prefix, "model_complex.rds"))
  model_complex_failed_file <- file.path(out_dir, paste0(prefix, "model_complex.failed"))

  if (file.exists(model_complex_file)) {
    message("   * Loading Complex Model from disk")
    return(readRDS(model_complex_file))
  } else if (file.exists(model_complex_failed_file)) {
    message("   * Complex Model previously failed. Skipping to next.")
  } else {
    message("   * Creating Complex Model")
    tryCatch({
      m <- lme(
        fixed = fixed_form,
        random = random_form,
        data = data,
        correlation = corAR1(form = ~ 1 | id),
        weights = varIdent(form = ~ 1 | level_change),
        control = lmeControl(opt = "nlminb", maxIter = 10000, msMaxIter = 10000, niterEM = 100, tolerance = 1e-3, msVerbose = TRUE)
      )
      message("   * Complex Model Converged! Saving to disk.")
      saveRDS(m, file = model_complex_file)
      capture.output(summary(m), file = file.path(out_dir, paste0(prefix, "model_complex_summary.txt")))
      return(m)
    }, error = function(e) {
      message("   ! Complex Model failed to converge: ", e$message)
      file.create(model_complex_failed_file)
    })
  }


  # ---------------------------------------------------------
  # ATTEMPT 2: Medium Model (Fallback 1)
  # ---------------------------------------------------------
  model_medium_file <- file.path(out_dir, paste0(prefix, "model_medium.rds"))
  model_medium_failed_file <- file.path(out_dir, paste0(prefix, "model_medium.failed"))

  if (file.exists(model_medium_file)) {
    message("   * Loading Medium Model from disk")
    return(readRDS(model_medium_file))
  } else if (file.exists(model_medium_failed_file)) {
    message("   * Medium Model previously failed. Skipping to next.")
  } else {
    message("   * Creating Medium Model")
    tryCatch({
      m <- lme(
        fixed = fixed_form,
        random = random_form,
        data = data,
        correlation = corAR1(form = ~ 1 | id),
        control = lmeControl(opt = "nlminb", maxIter = 10000, msMaxIter = 10000, niterEM = 100, tolerance = 1e-3, msVerbose = TRUE)
      )
      message("   * Medium Model Converged! Saving to disk.")
      saveRDS(m, file = model_medium_file)
      capture.output(summary(m), file = file.path(out_dir, paste0(prefix, "model_medium_summary.txt")))
      return(m)
    }, error = function(e) {
      message("   ! Medium Model failed to converge: ", e$message)
      file.create(model_medium_failed_file)
    })
  }



  # ---------------------------------------------------------
  # ATTEMPT 3: Low Model (Fallback 2)
  # ---------------------------------------------------------
  model_low_file <- file.path(out_dir, paste0(prefix, "model_low.rds"))
  model_low_failed_file <- file.path(out_dir, paste0(prefix, "model_low.failed"))

  if (file.exists(model_low_file)) {
    message("   * Loading Low Model from disk")
    return(readRDS(model_low_file))
  } else if (file.exists(model_low_failed_file)) {
    message("   * Low Model previously failed. Skipping.")
  } else {
    message("   * Creating Low Model")
    tryCatch({
      m <- lme(
        fixed = fixed_form,
        random = random_form,
        data = data,
        weights = varIdent(form = ~ 1 | level_change)
      )
      message("   * Low Model Converged! Saving to disk.")
      saveRDS(m, file = model_low_file)
      capture.output(summary(m), file = file.path(out_dir, paste0(prefix, "model_low_summary.txt")))
      return(m)
    }, error = function(e) {
      message("   ! Low Model failed to converge: ", e$message)
      file.create(model_low_failed_file)
    })
  }


  # ---------------------------------------------------------
  # ATTEMPT 4: Simple Model (Fallback 3)
  # ---------------------------------------------------------
  model_simple_file <- file.path(out_dir, paste0(prefix, "model_simple.rds"))
  model_simple_failed_file <- file.path(out_dir, paste0(prefix, "model_simple.failed"))

  if (file.exists(model_simple_file)) {
    message("   * Loading Simple Model from disk")
    return(readRDS(model_simple_file))
  } else if (file.exists(model_simple_failed_file)) {
    message("   * Simple Model previously failed. Skipping.")
  } else {
    message("   * Creating Simple Model")
    tryCatch({
      m <- lme(
        fixed = fixed_form,
        random = random_form,
        data = data,
        control = lmeControl(opt = "optim")
      )
      message("   * Simple Model Converged! Saving to disk.")
      saveRDS(m, file = model_simple_file)
      capture.output(summary(m), file = file.path(out_dir, paste0(prefix, "model_simple_summary.txt")))
      return(m)
    }, error = function(e) {
      message("   ! Simple Model failed to converge: ", e$message)
      file.create(model_simple_failed_file)
    })
  }

  # ---------------------------------------------------------
  # Final Fallback
  # ---------------------------------------------------------
  message("   !!! All models failed to converge or were previously marked as failed.")
  return(NULL)
}
