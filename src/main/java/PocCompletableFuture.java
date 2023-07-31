import lombok.Builder;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;

public class PocCompletableFuture {

  public static void main(String[] args) {
    new PocCompletableFuture().test0();
    new PocCompletableFuture().test1();
    new PocCompletableFuture().test2();
    new PocCompletableFuture().test3();
  }

  public void test0() {
    long start = System.currentTimeMillis();

    List<String> transactions = new ArrayList<>();

    buildTransactions()
            .forEach(transaction ->
                    ((Runnable) () -> transactions.add(process(transaction))).run());

    long end = System.currentTimeMillis();

    System.out.printf("The operation took %s ms%n", end - start);
    System.out.println("Transactions are: " + transactions);
  }

  public void test1() {
    long start = System.currentTimeMillis();

    List<String> transactions = buildTransactions()
            .parallel()
            .map(this::process)
            .collect(toList());

    long end = System.currentTimeMillis();

    System.out.printf("The operation took %s ms%n", end - start);
    System.out.println("Transactions are: " + transactions);
  }

  public void test2() {
    long start = System.currentTimeMillis();

    List<String> transactions = buildTransactions()
            .map(t -> CompletableFuture.supplyAsync(() -> process(t)))
            .collect(toList())
            .stream()
            .map(CompletableFuture::join)
            .collect(toList());

    long end = System.currentTimeMillis();

    System.out.printf("The operation took %s ms%n", end - start);
    System.out.println("Transactions are: " + transactions);
  }

  public void test3() {
    final ExecutorService executor = Executors.newFixedThreadPool(10);

    long start = System.currentTimeMillis();

    List<String> transactions = buildTransactions()
            .map(t -> supplyAsync(() -> process(t), executor))
            .collect(toList())
            .stream()
            .map(CompletableFuture::join)
            .collect(toList());

    long end = System.currentTimeMillis();

    System.out.printf("The operation took %s ms%n", end - start);
    System.out.println("Transactions are: " + transactions);

    executor.shutdown();
  }

  private static Stream<Transaction> buildTransactions() {
    return Stream.of(
            Transaction.builder().id(1).description("transaction 1").build(),
            Transaction.builder().id(2).description("transaction 2").build(),
            Transaction.builder().id(3).description("transaction 3").build(),
            Transaction.builder().id(4).description("transaction 4").build(),
            Transaction.builder().id(5).description("transaction 5").build(),
            Transaction.builder().id(6).description("transaction 6").build(),
            Transaction.builder().id(7).description("transaction 7").build(),
            Transaction.builder().id(8).description("transaction 8").build(),
            Transaction.builder().id(9).description("transaction 9").build(),
            Transaction.builder().id(10).description("transaction 10").build());
  }

  @SneakyThrows
  public String process(Transaction transaction) {
    System.out.println("processing ".concat(transaction.getDescription()));
    SECONDS.sleep((long) (Math.random() * 2));
    return transaction.toString();
  }

  @Getter
  @Builder
  static class Transaction {
    private int id;
    private String description;

    @Override
    public String toString() {
      return description;
    }
  }
}
