# ------------------------------------------------------------------------------

save_everything <- function(model, data, out_dir, linter_name, metric_data){
  dir.create(file.path(out_dir), showWarnings = FALSE, recursive = TRUE)

  transition_n_days <- 90

  metric_name         <- metric_data$name
  metric_display_name <- metric_data$display_name
  metric_unit         <- metric_data$unit


  # get the effects for each project
  random_effects <- ranef(model)
  fixed_effects <- fixef(model)

  # print model results
  capture.output(summary(model), file = file.path(out_dir, sprintf("%s.%s.model_summary.txt", linter_name, metric_display_name)))

  # print metric histogram
  save_png(
    expression = plot_raw_data_hist(data[[metric_name]], metric_display_name, metric_unit),
    file = file.path(out_dir, sprintf("%s.%s.hist.png", linter_name, metric_name))
  )

  # print fixed effects
  save_multipanel_png(
    expression = {
      plot_predicted(data, model,  0.1, metric_display_name, metric_unit, transition_n_days)
      plot_predicted(data, model,  0.5, metric_display_name, metric_unit, transition_n_days)
      plot_predicted(data, model,  1  , metric_display_name, metric_unit, transition_n_days)
      plot_predicted(data, model,  2  , metric_display_name, metric_unit, transition_n_days)
      plot_predicted(data, model,  4  , metric_display_name, metric_unit, transition_n_days)
      plot_predicted(data, model,  6  , metric_display_name, metric_unit, transition_n_days)
      plot_predicted(data, model,  8  , metric_display_name, metric_unit, transition_n_days)
      plot_predicted(data, model, 10  , metric_display_name, metric_unit, transition_n_days)
    },
    file = file.path(out_dir, sprintf("%s.%s.predicted.png", linter_name, metric_name)),
    height = 15,
    width = 40,
    mfrow = c(2,4)
  )

  # print qq of residuals
  save_png(
    expression = plot_residuals_qq(data, model),
    file = file.path(out_dir, sprintf("%s.%s.residual_qq.png", linter_name, metric_name))
  )

  # print residuals vs fitted
  save_png(
    expression = plot_residuals_vs_fitted(data, model),
    file = file.path(out_dir, sprintf("%s.%s.residual_vs_fitted.png", linter_name, metric_name))
  )

  # print residuals vs time
  save_png(
    expression = plot_residuals_vs_time_1(data, model),
    file = file.path(out_dir, sprintf("%s.%s.residual_vs_time.png", linter_name, metric_name))
  )

  # print residuals histogram
  save_png(
    expression = plot_residuals_histogram(data, model),
    file = file.path(out_dir, sprintf("%s.%s.residual_hist.png", linter_name, metric_name))
  )

  # print Auto- and Cross- Covariance and -Correlation Function Estimation
  save_png(
    expression = acf(resid(model, type = "normalized")),
    file = file.path(out_dir, sprintf("%s.%s.acf.png", linter_name, metric_name))
  )

  # print random effects correlation
  save_png(
    expression = plot_random_effects_correlation(model, metric_display_name),
    file = file.path(out_dir, sprintf("%s.%s.random_effects_correlation.png", linter_name, metric_name))
  )

  # print random effects histograms
  save_multipanel_png(
    expression = {
      plot_random_effects_histogram(random_effects$`(Intercept)`, "lightblue", "intercept", random_effects["(Intercept)"])
      plot_random_effects_histogram(random_effects$base_slope, "lightgreen", "base_slope", random_effects["base_slope"])
      plot_random_effects_histogram(random_effects$intervention_slope, "darkseagreen1", "intervention_slope", random_effects["intervention_slope"])
      plot_random_effects_histogram(random_effects$intervention_jump, "paleturquoise1", "intervention_jump", random_effects[4])
    },
    file = file.path(out_dir, sprintf("%s.%s.random_effect_hist.png", linter_name, metric_name)),
  )

  # print random effects qq-plot
  save_multipanel_png(
    expression = {
      qqnorm(random_effects[, "(Intercept)"], main = "intercept"); qqline(random_effects[, "(Intercept)"])
      qqnorm(random_effects[, "base_slope"], main = "base_slope"); qqline(random_effects[, "base_slope"])
      qqnorm(random_effects[, "intervention_slope"], main = "intervention_slope"); qqline(random_effects[, "intervention_slope"])
      qqnorm(random_effects[, "intervention_jump"], main = "intervention_jump"); qqline(random_effects[, "intervention_jump"])
    },
    file = file.path(out_dir, sprintf("%s.%s.random_effects_qq.png", linter_name, metric_name)),
  )

  # print random effects vs residuals
  effects <- c("(Intercept)", "base_slope", "intervention_slope", "intervention_jump")
  plots <- lapply(effects, function(eff) plot_random_effects_vs_residuals(data, model, eff))
  save_png(
    expression = print((plots[[1]] | plots[[2]]) / (plots[[3]] | plots[[4]])),
    file = file.path(out_dir, sprintf("%s.%s.random_effects_residuals.png", linter_name, metric_name)),
    width = 30,
    height = 20
  )

  # print prediction for project
  i <- 0
  project_out_dir <- paste(out_dir,"projects", sep = "/")
  dir.create(file.path(project_out_dir), showWarnings = FALSE, recursive = TRUE)
  for (id in unique(data[["id"]])) {
    save_png(
      expression = plot_project(model, id, data, metric_name, metric_unit, metric_display_name, transition_n_days),
      file = file.path(project_out_dir, sprintf("%s.%s.%s.png", linter_name, metric_name, id))
    )

    i <- i + 1
    if(i >= 30){
       # break
    }
  }
}

# ------------------------------------------------------------------------------

# ------------------------------------------------------------------------------
