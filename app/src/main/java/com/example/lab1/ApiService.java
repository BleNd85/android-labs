package com.example.lab1;

import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @GET("api/v1/expenses")
    Call<List<Expense>> getAllExpenses(@Query("page") int page, @Query("size") int size);

    @GET("api/v1/expenses/{id}")
    Call<Expense> getExpenseById(@Path("id") UUID id);

    @GET("api/v1/expenses/category/{category}")
    Call<List<Expense>> getExpensesByCategory(@Path("category") String category,
                                              @Query("page") int page,
                                              @Query("size") int size);

    @GET("api/v1/expenses/date")
    Call<List<Expense>> getExpensesByDateRange(@Query("dateBefore") String dateBefore,
                                               @Query("dateAfter") String dateAfter,
                                               @Query("page") int page,
                                               @Query("size") int size);

    @DELETE("api/v1/expenses/{id}")
    Call<Void> deleteExpense(@Path("id") UUID id);

    @POST("api/v1/expenses")
    Call<Expense> createExpense(@Body Expense expense);

    @PUT("api/v1/expenses/{id}")
    Call<Expense> updateExpense(@Path("id") UUID id, @Body Expense expense);

    @GET("api/v1/expenses/date/sum")
    Call<Double> getExpenseSumForRange(@Query("dateBefore") String dateBefore,
                                       @Query("dateAfter") String dateAfter);

    @GET("api/v1/expenses/sum")
    Call<Double> getTotalExpenseSum();

    @GET("api/v1/incomes")
    Call<List<Income>> getAllIncomes(@Query("page") int page, @Query("size") int size);

    @GET("api/v1/incomes/category/{category}")
    Call<List<Income>> getIncomesByCategory(@Path("category") String category,
                                            @Query("page") int page,
                                            @Query("size") int size);

    @GET("api/v1/incomes/date")
    Call<List<Income>> getIncomesByDateRange(@Query("dateBefore") String dateBefore,
                                             @Query("dateAfter") String dateAfter,
                                             @Query("page") int page,
                                             @Query("size") int size);

    @GET("api/v1/incomes/{id}")
    Call<Income> getIncomeById(@Path("id") UUID id);

    @DELETE("api/v1/incomes/{id}")
    Call<Void> deleteIncome(@Path("id") UUID id);

    @POST("api/v1/incomes")
    Call<Income> createIncome(@Body Income income);

    @PUT("api/v1/incomes/{id}")
    Call<Income> updateIncome(@Path("id") UUID id, @Body Income income);

    @GET("api/v1/incomes/date/sum")
    Call<Double> getIncomeSumForRange(@Query("dateBefore") String dateBefore,
                                      @Query("dateAfter") String dateAfter);

    @GET("api/v1/incomes/sum")
    Call<Double> getTotalIncomeSum();
}
