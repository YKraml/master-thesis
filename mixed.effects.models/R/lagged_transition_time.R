# ------------------------------------------------------------------------------

get_results <- function(metric_name, days){
  transition_times <- seq(from = 0, to = days, by = 7)
  results_list <- list()

  for (t_time in transition_times) {
    message(t_time)
    data_treatment <- get_data("treatment", t_time)
    f_full <- sprintf("%s ~ relative_introduction_time * (base_slope + intervention_jump + intervention_slope)", metric_name)
    model <- tryCatch({
      lme(
        fixed = as.formula(f_full),
        random = ~ 1 + base_slope + intervention_slope + intervention_jump | id,
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
