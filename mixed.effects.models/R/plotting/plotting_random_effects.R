# ------------------------------------------------------------------------------

plot_random_effects_vs_residuals <- function(data, model, effect) {
  # Residuals
  resid_df <- data.frame(
    id = data$id,
    residual = resid(model, type = "pearson")
  )

  # Random effects
  ranef_df <- tibble::rownames_to_column(ranef(model), "id")
  plot_df <- merge(resid_df, ranef_df, by = "id")

  plot_df$id <- factor(plot_df$id)
  ggplot(plot_df, aes(x = !!sym(effect), y = residual, color = id)) +
    geom_point() +
    labs(
      x = paste0("Random effect of ", effect, " (by id)"),
      y = "Residual",
      title = paste0("Residuals vs Random Effect of ", effect)
    ) +
    theme(legend.position = "none")
}

# ------------------------------------------------------------------------------

# ------------------------------------------------------------------------------

plot_random_effects_histogram <- function(data, color, main, fixed) {
  sigma <- sd(data)
  mean <- mean(data)
  left = mean - sigma * 2
  right = mean + sigma * 2
  hist(
    data[data > left & data < right],
    main = main,
    xlab = "Value",
    ylab = "Density",
    freq = TRUE,
    col = color,
    border = "black",
    breaks = 30,
    xlim = c(left, right)
  )
  abline(v = mean, col = "red", lwd = 2, lty = 2)

  # legend
  legend(
    "topright",
    legend = c(paste0("Mean: ", round(mean, 3)), paste0("Fixed: ", round(fixed, 3))),
    col = c("red", NA),
    lty = c(2, NA),
    pch = c(NA, NA))
}

# ------------------------------------------------------------------------------

# ------------------------------------------------------------------------------

plot_random_effects_correlation <- function(model){
  pairs(
    ranef(model),
    upper.panel = NULL,
    diag.panel = function(x) {
      par(new = TRUE)
      hist(x, col = "lightblue", border = "black", axes = FALSE, main = "")
      }
  )
  }


# ------------------------------------------------------------------------------
