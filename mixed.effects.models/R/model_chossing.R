file_name <-"data/bucket_one/ALL.csv"
data <- get_data_from_file(file_name)
metric <- "findings"


model_full <- lme(
  fixed = as.formula(paste0(metric, " ~ base_slope + intervention_slope + intervention_jump")),
  random = ~ 1 + base_slope + intervention_slope + intervention_jump | id,
  data = data,
  method = "ML",
  control = lmeControl(opt = "optim")
)


model_reduced <- lme(
  fixed = as.formula(paste0(metric, " ~ base_slope + intervention_slope + intervention_jump")),
  random = ~ 1 | id,
  data = data,
  method = "ML",
  control = lmeControl(opt = "optim")
)

anova(model_full, model_reduced)
