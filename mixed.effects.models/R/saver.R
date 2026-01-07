# ------------------------------------------------------------------------------

save_everything <- function(model, data, folder_name){
  dir.create(file.path(folder_name), showWarnings = FALSE, recursive = TRUE)

  # get the effects for each project
  random_effects <- ranef(model)
  fixed_effects <- fixef(model)
  absolute_effects <- sweep(random_effects, MARGIN = 2, STATS = fixed_effects, FUN = "+")

  # print model results
  capture.output(summary(model), file = file.path(folder_name, "model_summary.txt"))

  # print metric histogram
  save_png(
    expression = plot_raw_data_hist(data[[metric]], metric),
    file = file.path(folder_name, "hist.png")
  )

  # print fixed effects
  save_png(
    expression = plot_predicted_1(data, model),
    file = file.path(folder_name, "predicted.png")
  )

  # print qq of residuals
  save_png(
    expression = plot_residuals_qq(data, model),
    file = file.path(folder_name, "residuals_qq.png")
  )

  # print residuals vs fitted
  save_png(
    expression = plot_residuals_vs_fitted(data, model),
    file = file.path(folder_name, "residuals_vs_fitted.png")
  )

  # print residuals vs time
  save_png(
    expression = plot_residuals_vs_time_1(data, model),
    file = file.path(folder_name, "residuals_vs_time.png")
  )

  # print residuals histogram
  save_png(
    expression = plot_residuals_histogram(data, model),
    file = file.path(folder_name, "residuals_hist.png")
  )

  # print Auto- and Cross- Covariance and -Correlation Function Estimation
  save_png(
    expression = acf(resid(model, type = "normalized")),
    file = file.path(folder_name, "acf.png")
  )

  # print random effects correlation
  save_png(
    expression = plot_random_effects_correlation(model, metric),
    file = file.path(folder_name, "random_effects_correlation.png")
  )

  # print absolute effects histograms
  save_multipanel_png(
    expression = {
      plot_random_effects_histogram(absolute_effects$`(Intercept)`, "lightblue", "intercept", fixed_effects["(Intercept)"])
      plot_random_effects_histogram(absolute_effects$base_slope, "lightgreen", "base_slope", fixed_effects["base_slope"])
      plot_random_effects_histogram(absolute_effects$intervention_slope, "darkseagreen1", "intervention_slope", fixed_effects["intervention_slope"])
      plot_random_effects_histogram(absolute_effects$intervention_jump, "paleturquoise1", "intervention_jump", fixed_effects[4])
    },
    file = file.path(folder_name, "absolute_effects_hist.png"),
  )

  # print random effects histograms
  save_multipanel_png(
    expression = {
      plot_random_effects_histogram(random_effects$`(Intercept)`, "lightblue", "intercept", random_effects["(Intercept)"])
      plot_random_effects_histogram(random_effects$base_slope, "lightgreen", "base_slope", random_effects["base_slope"])
      plot_random_effects_histogram(random_effects$intervention_slope, "darkseagreen1", "intervention_slope", random_effects["intervention_slope"])
      plot_random_effects_histogram(random_effects$intervention_jump, "paleturquoise1", "intervention_jump", random_effects[4])
    },
    file = file.path(folder_name, "random_effects_hist.png"),
  )

  # print random effects qq-plot
  save_multipanel_png(
    expression = {
      qqnorm(random_effects[, "(Intercept)"], main = "intercept"); qqline(random_effects[, "(Intercept)"])
      qqnorm(random_effects[, "base_slope"], main = "base_slope"); qqline(random_effects[, "base_slope"])
      qqnorm(random_effects[, "intervention_slope"], main = "intervention_slope"); qqline(random_effects[, "intervention_slope"])
      qqnorm(random_effects[, "intervention_jump"], main = "intervention_jump"); qqline(random_effects[, "intervention_jump"])
    },
    file = file.path(folder_name, "random_effects_qq.png"),
  )

  # print random effects vs residuals
  effects <- c("(Intercept)", "base_slope", "intervention_slope", "intervention_jump")
  plots <- lapply(effects, function(eff) plot_random_effects_vs_residuals(data, model, eff))
  save_png(
    expression = print((plots[[1]] | plots[[2]]) / (plots[[3]] | plots[[4]])),
    file = file.path(folder_name, "random_effects_vs_residuals.png"),
    width = 30,
    height = 20
  )

  # print prediction for project
  project_folder_name <- paste(folder_name,"projects", sep = "/")
  dir.create(file.path(project_folder_name), showWarnings = FALSE, recursive = TRUE)
  # for (id in unique(data[["id"]])) {
  #   save_png(
  #     expression = plot_project_1(model, id, data),
  #     file = file.path(project_folder_name, paste0(id, ".png"))
  #   )
  # }
}

# ------------------------------------------------------------------------------

# ------------------------------------------------------------------------------
