

# ------------------------------------------------------------------------------

plot_predicted <- function(data, model, relative_introduction_time, metric, metric_unit, transition_n_days) {


  pred <- data.frame(x = seq(max(-2, -relative_introduction_time), 2, by = 0.01))
  pred <- subset(pred, x < 0 | x > transition_n_days / 365)

  pred$intervention_jump  <- ifelse(pred$x < 0, 0, 1)
  pred$base_slope         <- pred$x
  pred$intervention_slope <- pred$x * pred$intervention_jump
  pred$relative_introduction_time <- relative_introduction_time


  results <- mapply(function(rit, bs, ij, is) {
    get_predicted(model, rit, bs, ij, is)
  }, pred$relative_introduction_time, pred$base_slope, pred$intervention_jump, pred$intervention_slope)


  pred$pred  <- unlist(results[1, ])
  pred$lower <- unlist(results[3, ])
  pred$upper <- unlist(results[2, ])

  parameters <- get_parameters(model, relative_introduction_time)

  # 2. Extract values into a named list or vector for easier access
  # We use indexing based on the order: Intercept (1), Base (2), Jump (3), Slope (4)
  vals <- parameters$Value
  ps   <- parameters$p_value

  # --- 6. Prepare Plot Labels (Using the MARGINAL results) ---
  plot_text <- paste0(
    "Intercept = ",           round(vals[1], 3), " (p ", fmt_p(ps[1]), ")\n",
    "Base Slope = ",          round(vals[2], 3), " (p ", fmt_p(ps[2]), ")\n",
    "Intervention Jump = ",   round(vals[3], 3), " (p ", fmt_p(ps[3]), ")\n",
    "Intervention Slope = ",  round(vals[4], 3), " (p ", fmt_p(ps[4]), ")\n"
  )

  # --- 7. Plotting (Enhanced) ---

  # Setup Data & Limits
  ymin <- min(pred$lower, na.rm = TRUE)
  ymax <- max(pred$upper, na.rm = TRUE)
  yrange <- ymax - ymin
  # Increase top margin slightly for phase labels
  ylim <- c(ymin - yrange * 0.1, ymax + yrange * 0.5)

  gap_end <- transition_n_days / 365

  # Split data indices
  idx_pre <- which(pred$x <= 0)
  idx_post <- which(pred$x >= gap_end)

  # Initialize Plot
  par(mar = c(5, 5, 4, 2) + 0.1) # Add a little margin room
  plot(
    pred$x, pred$pred,
    type = "n",
    xlab = "Time (Years)",
    ylab = sprintf("%s (%s)", metric, metric_unit),
    ylim = ylim,
    main = paste0("Model Prediction (Intro Time: ", relative_introduction_time, ifelse(relative_introduction_time == 1, " year)", " years)")),
    cex.main = 1.1, cex.lab = 1.1, las = 1
  )

  # Add Grid (Optional, but helps readability)
  grid(nx = NULL, ny = NULL, col = "gray90", lty = "dotted")

  # Highlight the "Transition Phase" (The Gap)
  rect(
    xleft = 0,
    ybottom = par("usr")[3], # extend to bottom of plot area
    xright = gap_end,
    ytop = par("usr")[4],    # extend to top of plot area
    col = rgb(0.9, 0.9, 0.9, alpha = 0.4), # Very light grey
    border = NA
  )

  # Draw Confidence Intervals (Split)
  polygon(
    c(pred$x[idx_pre], rev(pred$x[idx_pre])),
    c(pred$lower[idx_pre], rev(pred$upper[idx_pre])),
    col = rgb(173, 216, 230, max = 255, alpha = 120), # Slightly more opaque
    border = NA
  )
  polygon(
    c(pred$x[idx_post], rev(pred$x[idx_post])),
    c(pred$lower[idx_post], rev(pred$upper[idx_post])),
    col = rgb(173, 216, 230, max = 255, alpha = 120),
    border = NA
  )

  # Draw Fitted Lines (Split) - Using 'firebrick' for a pro look
  lines(pred$x[idx_pre], pred$pred[idx_pre], col = "red", lwd = 2.5)
  lines(pred$x[idx_post], pred$pred[idx_post], col = "red", lwd = 2.5)

  # Intervention Lines
  abline(v = 0, lty = 2, col = "darkgrey", lwd = 1.5)
  abline(v = gap_end , lty = 2, col = "darkgrey", lwd = 1.5)

  # Stats Box
  txt_x <- min(pred$x)
  txt_y <- ylim[2]
  txt_w <- strwidth(plot_text, cex = 0.8)
  txt_h <- strheight(plot_text, cex = 0.8)
  rect(
    xleft   = txt_x,
    ybottom = txt_y - txt_h,
    xright  = txt_x + txt_w,
    ytop    = txt_y,
    col     = "white",
    border  = NA
  )

  text(x = txt_x, y = txt_y, labels = plot_text, adj = c(0, 1), cex = 0.8)

  # Legend
  legend(
    "topright",
    legend = c("Fitted Line", "95% CI", "Transition Start & End", "Transition"),
    col = c("red", rgb(173, 216, 230, max = 255, alpha = 120), "darkgrey", rgb(0.9, 0.9, 0.9, alpha = 0.4)),
    lty = c(1, 1, 2, 1),
    lwd = c(2.5, 10, 1.5, 10),
    cex = 0.9
  )
}

# ------------------------------------------------------------------------------

# ------------------------------------------------------------------------------

get_predicted <- function(model, relative_introduction_time, base_slope, intervention_jump, intervention_slope){

  # 1. Prepare New Data with the input parameters
  # We extract the response variable name to prevent errors in model.frame construction
  resp_name <- all.vars(formula(model))[1]

  newdata <- data.frame(
    relative_introduction_time = relative_introduction_time,
    base_slope = base_slope,
    intervention_jump = intervention_jump,
    intervention_slope = intervention_slope,
    id = "dummy" # Required for lme structure, but value is ignored for population predictions
  )

  # Add the dummy response variable
  newdata[[resp_name]] <- 0

  # 2. Create the Design Matrix (X)
  # This automatically handles the spline generation and interaction terms
  X <- model.matrix(terms(model), data = newdata)

  # 3. Calculate Prediction (Point Estimate)
  # Prediction = Design Matrix * Coefficients
  beta <- fixef(model)
  prediction <- as.numeric(X %*% beta)

  # 4. Calculate Confidence Intervals
  # Variance of prediction = X * V * X_transpose
  V <- vcov(model)
  se <- sqrt(as.numeric(diag(X %*% V %*% t(X))))

  # Get Degrees of Freedom (same logic as previous function)
  df <- tryCatch(summary(model)$tTable[1, "DF"], error = function(e) model$df.residual)

  # T-critical value for 95% CI (two-tailed)
  t_crit <- qt(0.975, df = df)

  # 5. Return List
  return(list(
    prediction = prediction,
    conf_upper = prediction + (t_crit * se),
    conf_lower = prediction - (t_crit * se)
  ))
}

# ------------------------------------------------------------------------------

# ------------------------------------------------------------------------------



get_parameters <- function(model, relative_introduction_time) {

  # 1. Extract Fixed Effects (Coefficients) and Variance-Covariance Matrix
  beta <- fixef(model)
  V <- vcov(model)

  # 2. Identify the response variable name (needed to satisfy model.matrix structure)
  # We extract the variable name from the left side of the formula
  resp_name <- all.vars(formula(model))[1]

  # 3. Create a 'newdata' frame representing 4 scenarios at the specific time t:
  #   Row 1: The Reference/Baseline (all dummies = 0)
  #   Row 2: Base Slope Active (base_slope = 1)
  #   Row 3: Intervention Jump Active (intervention_jump = 1)
  #   Row 4: Intervention Slope Active (intervention_slope = 1)

  newdata <- data.frame(
    relative_introduction_time = rep(relative_introduction_time, 4),
    base_slope = c(0, 1, 0, 0),
    intervention_jump = c(0, 0, 1, 0),
    intervention_slope = c(0, 0, 0, 1),
    id = "dummy_id" # Required for lme object structure, value doesn't matter here
  )

  # Add the response variable (set to 0) to prevent model.frame errors
  newdata[[resp_name]] <- 0

  # 4. Generate the Model Matrix (Design Matrix)
  # CRITICAL: We use terms(model) so that 'ns()' uses the EXACT same knots
  # and boundaries as the fitted model.
  X <- model.matrix(terms(model), data = newdata)

  # 5. Define Contrasts (Linear Combinations)
  # The "Value" of a parameter is the difference between the Active state and the Reference state.

  # Contrast for Intercept (The baseline value at time t)
  L_intercept <- X[1, ]

  # Contrast for Base Slope (Difference between Row 2 and Row 1)
  L_base_slope <- X[2, ] - X[1, ]

  # Contrast for Intervention Jump (Difference between Row 3 and Row 1)
  L_intervention_jump <- X[3, ] - X[1, ]

  # Contrast for Intervention Slope (Difference between Row 4 and Row 1)
  L_intervention_slope <- X[4, ] - X[1, ]

  # Combine into a matrix L
  L <- rbind(
    "(Intercept)" = L_intercept,
    "base_slope" = L_base_slope,
    "intervention_jump" = L_intervention_jump,
    "intervention_slope" = L_intervention_slope
  )

  # 6. Calculate Estimates, Standard Errors, and P-values
  # Estimate = L * beta
  estimates <- L %*% beta

  # Variance of linear combination = L * V * L_transpose
  # Standard Error = sqrt(Variance)
  se <- sqrt(diag(L %*% V %*% t(L)))

  # t-values
  t_vals <- estimates / se

  # Degrees of Freedom (DF)
  # For LME with large N, we can usually use the residual DF or the term DF.
  # Given your table has ~204,817 DF, we extract the Intercept DF as a safe approximation.
  # If extraction fails, we default to the residual DF.
  df_used <- tryCatch(summary(model)$tTable[1, "DF"], error = function(e) model$df.residual)

  # p-values (2-tailed)
  p_vals <- 2 * (1 - pt(abs(t_vals), df = df_used))

  # 7. Format the output
  results <- data.frame(
    Parameter = rownames(L),
    Value = as.numeric(estimates),
    Std.Error = as.numeric(se),
    DF = df_used,
    t_value = as.numeric(t_vals),
    p_value = as.numeric(p_vals)
  )

  # Optional: Rounding for cleaner display
  results$p_value <- round(results$p_value, 5)

  return(results)
}


