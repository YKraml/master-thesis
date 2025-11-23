import matplotlib.pyplot as pyplot
import numpy
import statsmodels.api as statsmodels
from pandas import DataFrame
from statsmodels.regression.linear_model import RegressionResults


def create_sub_plot(number_of_metrics: int, number_of_plots_per_metric: int):
    rows = number_of_metrics
    fig, axes = pyplot.subplots(rows, number_of_plots_per_metric, figsize=(5 * number_of_plots_per_metric, 3 * rows))
    return axes


def plot(linter_introduction_time: float, linter_name: str, metrics_and_models: dict[str, RegressionResults]):
    all_axes = create_sub_plot(len(metrics_and_models), 6)

    iterator: int = 0
    for metric in metrics_and_models:
        axes = all_axes[iterator]
        iterator = iterator + 1
        model = metrics_and_models[metric]

        df = model.model.data.frame

        plot0 = axes[0]
        plot0.hist(df[metric])
        plot0.set_title("{0} Distribution".format(metric))

        df_for_pred = model.model.data.frame.copy()
        predictions = model.get_prediction(df_for_pred).summary_frame(alpha=0.05)
        ci_lower = predictions['mean_ci_lower']
        ci_upper = predictions['mean_ci_upper']
        time_x = df_for_pred['time']

        plot1 = axes[1]
        metric = '{0}'.format(model.model.endog_names)
        df.plot.scatter(x='time', y=metric, ax=plot1, label='Observed')
        plot1.axvline(x=linter_introduction_time, linestyle='--', color='green',
                      label="{0} introduced".format(linter_name))
        plot1.fill_between(time_x, ci_lower, ci_upper, color='red', alpha=0.50, label='95% Confidence Interval')
        plot1.plot(time_x, model.fittedvalues, color='red', label='Fitted')
        plot1.set_title("Observed data and fitted line with 95% CI")
        plot1.legend(loc='upper left')

        residuals = numpy.array(model.fittedvalues) - numpy.array(df[metric])

        plot2 = axes[2]
        plot2.hist(residuals)
        plot2.set_title('Residuals Distribution')

        plot3 = axes[3]
        statsmodels.qqplot(residuals, line='45', ax=plot3)
        plot3.set_title('QQ Plot of Residuals against Normal Distribution')
        plot3.set_xlabel('Theoretical Quantiles')
        plot3.set_ylabel('Sample Quantiles (Residuals)')

        plot4 = axes[4]
        plot4.scatter(df['time'], residuals, color='blue', label='Residuals')
        plot4.axhline(0, linestyle="--", color="red", label='Zero Error')
        plot4.set_title('Residuals over Time')
        plot4.set_xlabel('Time')
        plot4.set_ylabel('Residuals')
        plot4.legend(loc='upper left')

        plot5 = axes[5]
        statsmodels.graphics.tsa.plot_pacf(residuals, lags=40, ax=plot5)
        plot5.set_title("Partial Autocorrelation Function (PACF)")
        plot5.set_xlabel("Lag")
        plot5.set_ylabel("Partial Autocorrelation")


def get_linters(linter_frame: DataFrame) -> list[tuple[int, str]]:
    linters = []
    for idx, row in linter_frame.iterrows():
        time = row.iloc[0]
        linter = row.iloc[1]
        linters.append((time, linter))
    return linters


def create_its_model(data: DataFrame, linter_introduction_time: float, metric: str) -> RegressionResults:
    # Create three new columns
    # Looks like this after: index, time, averageFileSloc, intervention, t_pre, t_post
    data['intervention'] = (data['time'] >= linter_introduction_time).astype(int)
    data['t_pre'] = data['time'] * (1 - data['intervention'])
    data['t_post'] = data['time'] * data['intervention']

    formula = '{0} ~ t_pre + t_post + intervention + 1'.format(metric)

    model = statsmodels.OLS.from_formula(formula, data=data).fit(
        cov_type='HAC',
        cov_kwds={'maxlags': 3}
    )

    # print("")
    # print(metric)
    # print(data)
    # print(data.to_string())
    # print(data[metric].describe())
    # print(model.summary())

    return model
