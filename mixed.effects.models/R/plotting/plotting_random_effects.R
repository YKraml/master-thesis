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
  mu <- mean(data)

  left <- mu - sigma * 3
  right <- mu + sigma * 3

  hist(
    data[data > left & data < right],
    main = main,
    xlab = "Value",
    ylab = "Density",
    freq = FALSE,          # scale histogram to density
    col = color,
    border = "black",
    breaks = 30,
    xlim = c(left, right)
  )

  # normal distribution curve
  x <- seq(left, right, length.out = 500)
  lines(x, dnorm(x, mean = mu, sd = sigma),
        col = "red", lwd = 2)

  # legend
  legend(
    "topright",
    legend = c("Normal density"),
    col = c("red"),
    lty = c(2),
    lwd = c(2),
  )
}

# ------------------------------------------------------------------------------

# ------------------------------------------------------------------------------

plot_random_effects_correlation <- function(model, metric, color) {

  # 1. Lower Panel: Scatter plot and Regression line ONLY
  panel.scatter <- function(x, y, ...) {
    points(x, y, pch = 4, col = color)

    fit <- lm(y ~ x)
    abline(fit, col = "red", lwd = 2)
  }

  # 2. Upper Panel: Text Statistics ONLY
  panel.txt <- function(x, y, ...) {

    # Calculate Statistics --
    ct      <- cor.test(x, y)
    fit     <- lm(y ~ x)
    sum_fit <- summary(fit)

    r       <- unname(ct$estimate)
    p_cor   <- ct$p.value

    slope   <- coef(fit)[2]
    p_slope <- sum_fit$coefficients[2, 4]

    legend_text <- c(
      paste0("r = ", round(r, 3)),
      paste0("Slope = ", round(slope, 2)),
      paste0("p ", fmt_p(p_cor))
    )

    full_text <- paste(legend_text, collapse = "\n")

    # Draw Text
    usr <- par("usr")
    on.exit(par(usr = usr))
    par(usr = c(0, 1, 0, 1))
    text(0.5, 0.5, full_text, cex = 1.2, col = "black")
  }

  random_effects <- ranef(model)


  # 4. Calculate Limits for the scatter plots
  lims <- apply(random_effects, 2, function(z) {
    mu <- mean(z, na.rm = TRUE)
    s  <- sd(z, na.rm = TRUE)
    c(mu - 3*s, mu + 3*s)
  })
  xlim <- range(lims, na.rm = TRUE)

  # 5. Generate the Pairs Plot
  pairs(
    random_effects,
    lower.panel = panel.scatter, # Scatter plots here
    upper.panel = panel.txt,     # Text stats here
    xlim = xlim,
    ylim = xlim,
    main = paste0("Correlation Matrix for: ", metric)
  )
}


# ------------------------------------------------------------------------------
