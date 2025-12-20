# ------------------------------------------------------------------------------

plot_predicted_1 <- function(data, model) {

  # Extract fixed effects and their covariance
  beta  <- fixef(model)
  Vb    <- vcov(model)

  # Create new time sequence
  new_time <- seq(-2, 2, by = 0.01)

  # Build new dataframe
  newdata <- data.frame(
    time = new_time,
    intervention = ifelse(new_time < 0, 0, 1)
  )

  # Construct predictors exactly as in your model
  newdata$t_pre  <- newdata$time * (1 - newdata$intervention)
  newdata$t_post <- newdata$time * newdata$intervention

  # Create the model matrix for fixed effects
  X <- model.matrix(~ 1 + t_pre + t_post + intervention, data = newdata)

  # Predicted values (fixed effects only)
  newdata$pred <- X %*% beta

  # Standard error of predictions
  newdata$se <- sqrt(diag(X %*% Vb %*% t(X)))

  # Confidence intervals (95%)
  newdata$lower <- newdata$pred - 1.96 * newdata$se
  newdata$upper <- newdata$pred + 1.96 * newdata$se

  # Calculate current y-range
  ymin <- min(newdata$lower)   # CI lower bound
  ymax <- max(newdata$upper)   # CI upper bound

  # Expand by a factor of 2
  yrange <- ymax - ymin
  ymin2 <- ymin - yrange / 2
  ymax2 <- ymax + yrange / 2

  # ---- Plot with confidence band ----
  plot(
    newdata$time, newdata$pred, type = "l",
    xlab = "Time in years", ylab = "Predicted metric",
    xlim = c(-2, 2),
    ylim = c(ymin2, ymax2),
    main = "Fitted fixed effect with 95% confidence interval"
  )

  # Add confidence interval ribbon
  polygon(
    c(newdata$time, rev(newdata$time)),
    c(newdata$upper, rev(newdata$lower)),
    col = adjustcolor("gray70", alpha.f = 0.5),
    border = NA
  )

  # intervention line
  abline(v = 0, lty = 2, col = "darkgrey")
}

# ------------------------------------------------------------------------------

# ------------------------------------------------------------------------------

plot_predicted_2 <- function(data, model){

  # Extract data from model
  fixed_effects <- fixef(model)
  v_matrix <- vcov(model)

  # Create new data frame for plotting
  new_data <- data.frame(
    x = seq(0, 2, by = 0.01)
  )
  new_data$y <- fixed_effects["(Intercept)"] + (new_data$x * fixed_effects["t_post"])
  pred <- ggpredict(model, terms = "t_post [0:2 by=0.01]")


  # Calculate y range
  ymin <- min(pred$conf.low)
  ymax <- max(pred$conf.high)
  yrange <- ymax - ymin
  ymin2 <- ymin - yrange / 2
  ymax2 <- ymax + yrange / 2

  # plot graph
  plot(
    new_data$x, new_data$y, type = "l",
    xlab = "Time in years", ylab = "Predicted metric",
    xlim = c(0, 2),
    ylim = c(ymin2, ymax2),
    main = "Fitted fixed effect with 95% confidence interval"
  )

  # plot ribbon
  polygon(
    c(pred$x, rev(pred$x)),
    c(pred$conf.high, rev(pred$conf.low)),
    col = adjustcolor("gray70", alpha.f = 0.5),
    border = NA
  )

  # intervention line
  abline(v = 0, lty = 2, col = "darkgrey")
}

# ------------------------------------------------------------------------------
