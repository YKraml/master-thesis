

# ------------------------------------------------------------------------------

plot_predicted <- function(data, model, relative_introduction_time, metric, metric_unit, transition_n_days) {

  # --- 1. Helper Function for Marginal Effects ---
  # Calculates: Beta_main + (Beta_interaction * relative_time)
  # And computes the correct new Standard Error and P-value
  get_marginal <- function(main_term, interact_term, val_rel_time, model_fixef, model_vcov, model_df) {

    # 1. Get Coefficients
    b_main   <- if(main_term %in% names(model_fixef)) model_fixef[[main_term]] else 0
    b_inter  <- if(interact_term %in% names(model_fixef)) model_fixef[[interact_term]] else 0

    # 2. Calculate the combined estimate (The "Effective Slope/Intercept")
    estimate <- b_main + (b_inter * val_rel_time)

    # 3. Calculate Variance for SE
    # Var(A + Bx) = Var(A) + x^2*Var(B) + 2x*Cov(A,B)
    var_main  <- if(main_term %in% rownames(model_vcov)) model_vcov[main_term, main_term] else 0
    var_inter <- if(interact_term %in% rownames(model_vcov)) model_vcov[interact_term, interact_term] else 0
    cov_mi    <- if(main_term %in% rownames(model_vcov) && interact_term %in% rownames(model_vcov)) model_vcov[main_term, interact_term] else 0

    variance <- var_main + (val_rel_time^2 * var_inter) + (2 * val_rel_time * cov_mi)
    se       <- sqrt(variance)

    # 4. Calculate P-value
    t_stat <- estimate / se
    # We use the DF from the main term as a safe approximation
    df     <- if(main_term %in% rownames(model_df)) model_df[main_term, "DF"] else 100
    p_val  <- 2 * (1 - pt(abs(t_stat), df))

    return(list(est = estimate, p = p_val))
  }

  # --- 2. Extract Model Data ---
  fixed_effects <- fixef(model)
  covariance    <- vcov(model)
  t_table       <- summary(model)$tTable # Used for Degrees of Freedom (DF)

  # --- 3. Calculate Marginal Effects ---
  # Note: R might name interactions "A:B" or "B:A". We check both.
  find_interaction <- function(term1, term2) {
    n1 <- paste0(term1, ":", term2)
    n2 <- paste0(term2, ":", term1)
    if(n1 %in% names(fixed_effects)) return(n1)
    if(n2 %in% names(fixed_effects)) return(n2)
    return(NULL) # Return NULL if no interaction found
  }

  # A. Intercept
  # (The "Interaction" for the intercept is the main effect of relative_introduction_time itself)
  res_intercept <- get_marginal("(Intercept)", "relative_introduction_time", relative_introduction_time, fixed_effects, covariance, t_table)

  # B. Base Slope
  int_name <- find_interaction("base_slope", "relative_introduction_time")
  res_base <- get_marginal("base_slope", int_name, relative_introduction_time, fixed_effects, covariance, t_table)

  # C. Intervention Jump
  int_name <- find_interaction("intervention_jump", "relative_introduction_time")
  res_jump <- get_marginal("intervention_jump", int_name, relative_introduction_time, fixed_effects, covariance, t_table)

  # D. Intervention Slope
  int_name <- find_interaction("intervention_slope", "relative_introduction_time")
  res_intslope <- get_marginal("intervention_slope", int_name, relative_introduction_time, fixed_effects, covariance, t_table)

  # --- 4. Create Prediction Data ---
  pred <- data.frame(x = seq(max(-2, -relative_introduction_time), 2, by = 0.01))
  pred <- subset(pred, x < 0 | x > transition_n_days / 365)

  pred$intervention_jump  <- ifelse(pred$x < 0, 0, 1)
  pred$base_slope         <- pred$x
  pred$intervention_slope <- pred$x * pred$intervention_jump
  pred$relative_introduction_time <- relative_introduction_time

  # --- 5. Generate Predictions ---
  terms_no_response <- delete.response(terms(model))
  X <- model.matrix(terms_no_response, data = pred)

  pred$pred <- X %*% fixed_effects
  pred$se <- sqrt(diag(X %*% covariance %*% t(X)))
  pred$lower <- pred$pred - 1.96 * pred$se
  pred$upper <- pred$pred + 1.96 * pred$se

  # --- 6. Prepare Plot Labels (Using the MARGINAL results) ---
  plot_text <- paste0(
    "Intercept = ",        round(res_intercept$est, 3), " (p ", fmt_p(res_intercept$p), ")\n",
    "Base Slope = ",       round(res_base$est, 3),      " (p ", fmt_p(res_base$p), ")\n",
    "Intervention Jump = ", round(res_jump$est, 3),      " (p ", fmt_p(res_jump$p), ")\n",
    "Intervention Slope = ", round(res_intslope$est, 3), " (p ", fmt_p(res_intslope$p), ")\n"
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


