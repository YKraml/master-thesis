# ------------------------------------------------------------------------------

plot_treated_vs_control_effects <- function(vals_control, vals_treated, plot_title) {

  # 1. Run Statistical Test (Welch Two Sample t-test)


  test_res <- t.test(vals_treated, vals_control, alternative = "less")

  # Extract stats for the text box
  mean_control <- mean(vals_control, na.rm = TRUE)
  mean_treated <- mean(vals_treated, na.rm = TRUE)
  pval         <- test_res$p.value

  # Calculate N (Sample Size)
  # We count valid (non-NA) entries
  n_control <- sum(!is.na(vals_control))
  n_treated <- sum(!is.na(vals_treated))

  # Create the label text
  # Added the "n(C)=..., n(T)=..." line at the bottom
  stats_text <- paste0(
    "Control Mean: ", round(mean_control, 3), "\n",
    "Treated Mean: ", round(mean_treated, 3), "\n",
    "p ", fmt_p(pval), " (one-sided less)\n",
    "n(C) = ", n_control, ", n(T) = ", n_treated
  )

  # 2. Calculate Densities
  d_control <- density(vals_control, na.rm = TRUE)
  d_treated <- density(vals_treated, na.rm = TRUE)

  # 3. Calculate Limits (Mean +/- 2 SD)
  all_vals <- c(vals_control, vals_treated)
  mu       <- mean(all_vals, na.rm = TRUE)
  sigma    <- sd(all_vals, na.rm = TRUE)

  x_lim_zoomed <- c(mu - 2 * sigma, mu + 2 * sigma)

  # 4. Calculate Y-axis scaling for the zoomed window
  max_y_control <- max(d_control$y[d_control$x >= x_lim_zoomed[1] & d_control$x <= x_lim_zoomed[2]])
  max_y_treated <- max(d_treated$y[d_treated$x >= x_lim_zoomed[1] & d_treated$x <= x_lim_zoomed[2]])

  if(is.infinite(max_y_control)) max_y_control <- max(d_control$y)
  if(is.infinite(max_y_treated)) max_y_treated <- max(d_treated$y)

  y_lim_zoomed <- c(0, max(max_y_control, max_y_treated) * 1.1)

  # Colors
  gg_red      <- rgb(248, 118, 109, maxColorValue = 255, alpha = 128)
  border_red  <- rgb(248, 118, 109, maxColorValue = 255)
  gg_blue     <- rgb(0, 191, 196, maxColorValue = 255, alpha = 128)
  border_blue <- rgb(0, 191, 196, maxColorValue = 255)

  # 5. Plot Canvas
  plot(d_control,
       main = plot_title,
       xlab = "Value",
       ylab = "Density",
       xlim = x_lim_zoomed,
       ylim = y_lim_zoomed,
       type = "n",
       bty = "l",
       las = 1)

  grid(nx = NULL, ny = NULL, col = "gray90", lty = "dotted")

  # 6. Draw Polygons
  polygon(d_control, col = gg_red, border = border_red)
  polygon(d_treated, col = gg_blue, border = border_blue)

  # 7. Add Stats Box (Top Left Corner)
  usr <- par("usr")

  txt_x <- usr[1] + strwidth(stats_text, cex = 0.8) * 0.1
  txt_y <- usr[4]

  txt_w <- strwidth(stats_text, cex = 0.8)
  txt_h <- strheight(stats_text, cex = 0.8)

  # Draw White Rectangle (Background)
  rect(
    xleft   = txt_x,
    ybottom = txt_y - txt_h * 1.2, # Slightly adjusted padding for the extra line
    xright  = txt_x + txt_w,
    ytop    = txt_y,
    col     = "white",
    border  = NA
  )

  # Draw Text
  text(x = txt_x, y = txt_y, labels = stats_text, adj = c(0, 1), cex = 0.8)

  # 8. Legend (Top Right)
  legend("topright",
         legend = c("Control", "Treated"),
         fill = c(gg_red, gg_blue),
         border = c(border_red, border_blue),
         bty = "n",
         cex = 0.8)
}

# ------------------------------------------------------------------------------


# ------------------------------------------------------------------------------


calculate_custom_effects <- function(model, data) {
  ids <- unique(data$id)
  all_ranefs <- ranef(model)


  results_list <- list()
  for (id in ids) {

    id_str <- as.character(id)
    projectData <- data[data$id == id, ]


    relative_introduction_time <- projectData[["relative_introduction_time"]][1]

    randomEffectsFromProject <- all_ranefs[id_str, , drop = FALSE]


    parameters <- get_parameters(model, relative_introduction_time)

    intercept          <- randomEffectsFromProject[["(Intercept)"]]        + parameters$Value[1]
    base_slope         <- randomEffectsFromProject[["base_slope"]]         + parameters$Value[2]
    intervention_jump  <- randomEffectsFromProject[["intervention_jump"]]  + parameters$Value[3]
    intervention_slope <- randomEffectsFromProject[["intervention_slope"]] + parameters$Value[4]

    slope_after_intervention <- base_slope + intervention_slope

    results_list[[id_str]] <- data.frame(
      id = id,
      intercept = intercept,
      base_slope = base_slope,
      intervention_jump = intervention_jump,
      slope_after_intervention = slope_after_intervention
    )
  }

  return(do.call(rbind, results_list))
}

# ------------------------------------------------------------------------------
