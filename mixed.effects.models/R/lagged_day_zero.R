# ------------------------------------------------------------------------------

get_lagged_dataset <- function(df, lag_weeks) {

  # We start by sorting the data
  df_ordered <- df %>% arrange(id, time)

  # Apply the lag or lead based on the scalar value of 'lag_weeks'
  if (lag_weeks > 0) {
    # POSITIVE LAG: Shift intervention later (lag)
    df_lagged <- df_ordered %>%
      group_by(id) %>%
      mutate(
        level_change = dplyr::lag(level_change, n = lag_weeks, default = 0)
      ) %>%
      ungroup()

  } else if (lag_weeks < 0) {
    # NEGATIVE LAG (LEAD): Shift intervention earlier (lead)
    # We must use abs() because n must be positive
    df_lagged <- df_ordered %>%
      group_by(id) %>%
      mutate(
        level_change = dplyr::lead(level_change, n = abs(lag_weeks), default = 1)
      ) %>%
      ungroup()

  } else {
    # ZERO LAG: Do nothing to the jump column
    df_lagged <- df_ordered
  }

  # Finally, recalculate the slope interaction for all rows
  # This step is common to all conditions, so we do it at the end
  df_lagged %>%
    mutate(
      slope_change = level_change * (base_slope - lag_weeks / (365 / 7))
    ) %>%
    filter(!is.na(level_change)) # Safety cleanup
}

# ------------------------------------------------------------------------------






# ------------------------------------------------------------------------------

compare_lagged_models <- function(data, metric, min_lag = -5, max_lag = 10) {

  model_results <- list()
  f_string <- paste0(metric, " ~ 1 + base_slope + level_change + slope_change")

  message(paste0("   * Starting comparison for lags ", min_lag, " to ", max_lag))

  for (i in min_lag:max_lag) {
    message(paste0("   * Fitting Lag ", i))

    # 1. Get the dataset (Fixed function call)
    lagged_data <- get_lagged_dataset(data, lag_weeks = i)

    # 2. Fit model
    model_fit <- tryCatch({
      lme(
        fixed = as.formula(f_string),
        random = ~ 1 + base_slope + slope_change + level_change | id,
        data = lagged_data,
        method = "ML",
        control = lmeControl(opt = "optim")
      )
    }, error = function(e) {
      message(paste("   * Error in Lag", i, ":", e$message))
      return(NULL)
    })

    # 3. Store results
    if (!is.null(model_fit)) {
      model_results[[paste0("lag_", i)]] <- data.frame(
        lag = i,
        AIC = AIC(model_fit),
        BIC = BIC(model_fit),
        logLik = as.numeric(logLik(model_fit)),
        converged = TRUE
      )
    } else {
      model_results[[paste0("lag_", i)]] <- data.frame(
        lag = i,
        AIC = NA,
        BIC = NA,
        logLik = NA,
        converged = FALSE
      )
    }
  }

  results_df <- do.call(rbind, model_results) %>%
    arrange(AIC)

  return(results_df)
}

# ------------------------------------------------------------------------------
