# ------------------------------------------------------------------------------

plot_model_sensitivity <- function(model) {
  time_values <- seq(0.1, 10, length.out = 100)

  param_list <- lapply(time_values, function(t) {
    params <- get_parameters(model, t)
    params$time <- t
    return(params)
  })


  plot_data <- do.call(rbind, param_list)
  unique_params <- unique(plot_data$Parameter)

  par(mfrow = c(2, 2))
  for (p in unique_params) {
    sub_data <- plot_data[plot_data$Parameter == p, ]

    plot(sub_data$time, sub_data$Value, type = "l", lwd = 2,
         xlab = "Project Age at Linter Introduction (Years)",
         ylab = "Parameter Estimate",
         main = p,
         col = "red")

    grid(nx = NULL, ny = NULL, col = "gray90", lty = "dotted")
  }

  par(mfrow = c(1,1))
}

# ------------------------------------------------------------------------------
