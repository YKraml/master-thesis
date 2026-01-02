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

  # vertical line at mean
  abline(v = mu, col = "red", lwd = 2, lty = 2)

  # legend
  legend(
    "topright",
    legend = c(
      paste0("Mean: ", round(mu, 3)),
      "Normal density"
    ),
    col = c("red", "red"),
    lty = c(2, 1),
    lwd = c(2, 2),
  )
}

# ------------------------------------------------------------------------------

# ------------------------------------------------------------------------------

plot_random_effects_correlation <- function(model) {

  # Panel for scatter and regression line
  panel.cor <- function(x, y, ...) {

    # scatter plot
    points(x, y, pch = 3, col = "lightblue")

    # linear model
    fit <- lm(y ~ x)
    abline(fit, col = "red", lwd = 2)

    # correlation test
    ct <- cor.test(x, y)

    # extract statistics
    r      <- unname(ct$estimate)
    p      <- ct$p.value
    ci     <- ct$conf.int
    coef   <- coef(fit)
    r2     <- summary(fit)$r.squared
    n      <- sum(complete.cases(x, y))

    # legend text
    legend_text <- c(
      # paste0("n = ", n),
      paste0("r = ", round(r, 3)),
      paste0("p = ", signif(p, 3))
      # paste0("95% CI [", round(ci[1], 3), ", ", round(ci[2], 3), "]")
      # paste0("slope = ", round(coef[2], 3))
      # paste0("intercept = ", round(coef[1], 3))
      # paste0("R² = ", round(r2, 3))
    )

    # legend
    legend(
      "topleft",
      legend = legend_text,
      cex = 0.75,
      text.col = "black"
    )
  }

  # Panel for histograms
  panel.hist <- function(x, ...) {
    usr <- par("usr")
    on.exit(par(usr = usr))

    par(usr = c(usr[1:2], 0, 1.5))

    h <- hist(x, plot = FALSE)
    y <- h$counts / max(h$counts)

    rect(
      h$breaks[-length(h$breaks)], 0,
      h$breaks[-1], y,
      col = "lightblue", border = "black"
    )
  }

  random_effects <- ranef(model)
  fixed_effects <- fixef(model)
  absolute_effects <- sweep(random_effects, MARGIN = 2, STATS = fixed_effects, FUN = "+")

  pairs(
    absolute_effects,
    upper.panel = NULL,
    lower.panel = panel.cor,
    diag.panel  = panel.hist
  )
}


# ------------------------------------------------------------------------------
