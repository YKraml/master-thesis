# ------------------------------------------------------------------------------

get_lagged_results <- function(language_name, metric_name, days){
  transition_times <- seq(from = 0, to = days, by = 7)
  results_list <- list()

  for (t_time in transition_times) {
    message(paste0(" - ", t_time))
    data_treatment <- get_data(language_name, "treatment", t_time)
    f_full <- sprintf("%s ~ base_slope + level_change + slope_change", metric_name)
    model <- tryCatch({
      lme(
        fixed = as.formula(f_full),
        random = ~ 1 + base_slope + level_change + slope_change | id,
        data = data_treatment,
        # correlation = corAR1(form = ~ t | id),
        method = "ML",
        control = lmeControl(opt = "optim")
      )
    }, error = function(e) {
      message(paste("Model failed to converge at transition time:", t_time))
      return(NULL)
    })

    if (!is.null(model)) {
      fe <- fixef(model)
      results_list[[length(results_list) + 1]] <- c(TransitionTime = t_time, fe)
    }
  }

  return(results_list)
}

# ------------------------------------------------------------------------------
