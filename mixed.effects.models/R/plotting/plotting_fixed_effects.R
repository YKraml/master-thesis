

# ------------------------------------------------------------------------------

plot_predicted <- function(data, model, relative_introduction_time, metric_display_name, metric_unit, transition_n_days, interaction) {


  pred <- data.frame(x = seq(max(-2, -relative_introduction_time), 2, by = 0.01))
  pred <- subset(pred, x < 0 | x > transition_n_days / 365)

  pred$level_change  <- ifelse(pred$x < 0, 0, 1)
  pred$base_slope         <- pred$x
  pred$slope_change <- pred$x * pred$level_change
  pred$relative_introduction_time <- relative_introduction_time


  results <- mapply(function(rit, bs, ij, is) {
    get_predicted(model, rit, bs, ij, is, interaction)
  }, pred$relative_introduction_time, pred$base_slope, pred$level_change, pred$slope_change)


  pred$pred  <- unlist(results[1, ])
  pred$lower <- unlist(results[3, ])
  pred$upper <- unlist(results[2, ])

  parameters <- get_parameters(model, relative_introduction_time, interaction)

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
    ylab = sprintf("%s (%s)", metric_display_name, metric_unit),
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

get_predicted <- function(model, relative_introduction_time, base_slope, level_change, slope_change, interaction) {



  if(interaction == "time_saturated"){
    # 1. Calculate the transformed time variable used in the model
    time_saturated <- 1 - exp(-1 * relative_introduction_time)

    # 2. Create the design vector (X) matching the order of fixed effects:
    # (Intercept), time_saturated, base_slope, level_change, slope_change,
    # time_saturated:base_slope, time_saturated:level_change, time_saturated:slope_change
    new_data_vec <- c(
      1,                                 # (Intercept)
      time_saturated,                    # time_saturated
      base_slope,                        # base_slope
      level_change,                 # level_change
      slope_change,                # slope_change
      time_saturated * base_slope,       # time_saturated:base_slope
      time_saturated * level_change,# time_saturated:level_change
      time_saturated * slope_change# time_saturated:slope_change
    )

    # 3. Get fixed effects coefficients and variance-covariance matrix
    beta <- nlme::fixef(model)
    V_cov <- vcov(model)

    # 4. Calculate point prediction: y = X * beta
    prediction <- sum(new_data_vec * beta)

    # 5. Calculate Standard Error for the prediction: SE = sqrt(X %*% V %*% t(X))
    # This accounts for the correlations between the fixed effects coefficients
    se_pred <- sqrt(t(new_data_vec) %*% V_cov %*% new_data_vec)

    # 6. Calculate 95% CI (using 1.96 for normal approximation)
    upper <- prediction + (1.96 * se_pred)
    lower <- prediction - (1.96 * se_pred)

    return(list(
      prediction = as.numeric(prediction),
      upper = as.numeric(upper),
      lower = as.numeric(lower)
    ))
  }


  if(interaction == "relative_introduction_time"){
    relative_introduction_time <- relative_introduction_time

    new_data_vec <- c(
      1,
      relative_introduction_time,
      base_slope,
      level_change,
      slope_change,
      relative_introduction_time * base_slope,
      relative_introduction_time * level_change,
      relative_introduction_time * slope_change
    )

    # 3. Get fixed effects coefficients and variance-covariance matrix
    beta <- nlme::fixef(model)
    V_cov <- vcov(model)

    # 4. Calculate point prediction: y = X * beta
    prediction <- sum(new_data_vec * beta)

    # 5. Calculate Standard Error for the prediction: SE = sqrt(X %*% V %*% t(X))
    # This accounts for the correlations between the fixed effects coefficients
    se_pred <- sqrt(t(new_data_vec) %*% V_cov %*% new_data_vec)

    # 6. Calculate 95% CI (using 1.96 for normal approximation)
    upper <- prediction + (1.96 * se_pred)
    lower <- prediction - (1.96 * se_pred)

    return(list(
      prediction = as.numeric(prediction),
      upper = as.numeric(upper),
      lower = as.numeric(lower)
    ))
  }




}

# ------------------------------------------------------------------------------

# ------------------------------------------------------------------------------



get_parameters <- function(model, relative_introduction_time, interaction) {

  if(interaction == "time_saturated"){
    int <- 1 - exp(-1 * relative_introduction_time)

    # 2. Extract fixed effects coefficients
    beta <- fixef(model)

    # 3. Define the "Effective" Values
    # Each parameter is: Main_Effect + (Interaction_Effect * t_sat)
    params <- c(
      intercept    = beta["(Intercept)"] + beta["time_saturated"] * int,
      base_slope   = beta["base_slope"] + beta["time_saturated:base_slope"] * int,
      level_change = beta["level_change"] + beta["time_saturated:level_change"] * int,
      slope_change = beta["slope_change"] + beta["time_saturated:slope_change"] * int
    )

    # 4. Calculate Standard Errors and P-values
    # We use the Delta Method logic: Var(A + k*B) = Var(A) + k^2*Var(B) + 2*k*Cov(A,B)
    V <- vcov(model)
    df <- model$fixDF$X # Degrees of freedom from the model

    # Mapping parameters to their component indices in the vcov matrix
    mapping <- list(
      intercept     = c("(Intercept)", "time_saturated"),
      base_slope    = c("base_slope", "time_saturated:base_slope"),
      level_change  = c("level_change", "time_saturated:level_change"),
      slope_change  = c("slope_change", "time_saturated:slope_change")
    )

    results <- data.frame(Parameter = names(params), Value = as.numeric(params), Std.Error = NA, p_value = NA)

    for (i in seq_along(mapping)) {
      p_main <- mapping[[i]][1]
      p_inter <- mapping[[i]][2]

      # Calculate combined variance
      combined_var <- V[p_main, p_main] + (int^2 * V[p_inter, p_inter]) + (2 * int * V[p_main, p_inter])
      se <- sqrt(combined_var)

      # Calculate t-stat and p-value
      t_val <- params[i] / se
      p_val <- 2 * pt(abs(t_val), df = df[p_main], lower.tail = FALSE)

      results$Std.Error[i] <- se
      results$p_value[i] <- p_val
    }

    return(results)
  }




  if(interaction == "relative_introduction_time"){
    int <- 1 - relative_introduction_time

    # 2. Extract fixed effects coefficients
    beta <- fixef(model)

    # 3. Define the "Effective" Values
    # Each parameter is: Main_Effect + (Interaction_Effect * t_sat)
    params <- c(
      intercept    = beta["(Intercept)"] + beta["relative_introduction_time"] * int,
      base_slope   = beta["base_slope"] + beta["relative_introduction_time:base_slope"] * int,
      level_change = beta["level_change"] + beta["relative_introduction_time:level_change"] * int,
      slope_change = beta["slope_change"] + beta["relative_introduction_time:slope_change"] * int
    )

    # 4. Calculate Standard Errors and P-values
    # We use the Delta Method logic: Var(A + k*B) = Var(A) + k^2*Var(B) + 2*k*Cov(A,B)
    V <- vcov(model)
    df <- model$fixDF$X # Degrees of freedom from the model

    # Mapping parameters to their component indices in the vcov matrix
    mapping <- list(
      intercept     = c("(Intercept)", "relative_introduction_time"),
      base_slope    = c("base_slope", "relative_introduction_time:base_slope"),
      level_change  = c("level_change", "relative_introduction_time:level_change"),
      slope_change  = c("slope_change", "relative_introduction_time:slope_change")
    )

    results <- data.frame(Parameter = names(params), Value = as.numeric(params), Std.Error = NA, p_value = NA)

    for (i in seq_along(mapping)) {
      p_main <- mapping[[i]][1]
      p_inter <- mapping[[i]][2]

      # Calculate combined variance
      combined_var <- V[p_main, p_main] + (int^2 * V[p_inter, p_inter]) + (2 * int * V[p_main, p_inter])
      se <- sqrt(combined_var)

      # Calculate t-stat and p-value
      t_val <- params[i] / se
      p_val <- 2 * pt(abs(t_val), df = df[p_main], lower.tail = FALSE)

      results$Std.Error[i] <- se
      results$p_value[i] <- p_val
    }

    return(results)
  }



}
