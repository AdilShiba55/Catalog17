import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("main-connection");
        EntityManager manager = factory.createEntityManager();

        Scanner in = new Scanner(System.in);

        System.out.println("Введите 1 для добавления товара" + "\nВведите 2 для изменения товара");
        String doing = in.nextLine();
        if(doing.isEmpty()) {
            System.out.println("Всего доброго!");
            return;
        }
        while(Integer.parseInt(doing) != 1 && Integer.parseInt(doing) != 2) {
            System.out.println("Введите 1 для добавления товара" + "\nВведите 2 для изменения товара");
            doing = in.nextLine();
        }

        if(Integer.parseInt(doing) == 1) {
            Product product = new Product();
            System.out.println("Введите ID категории");
            String categoryNumber = in.nextLine();

            while(manager.find(Category.class, Long.parseLong(categoryNumber)) == null) {
                System.out.println("Категории под данным числом не существует");
                categoryNumber = in.nextLine();
            }
            product.setCategory(manager.find(Category.class, Long.parseLong(categoryNumber))); //СЕТ КАТЕГОРИ

            System.out.println("Введите наименование товара");
            String productName = in.nextLine();
            if (!productName.isEmpty()) {
                product.setName(productName); //СЕТ НЕЙМ
            } else {
                while (productName.isEmpty()) {
                    System.out.println("Вы ввели пустую строку!" + "\nПопробуйте еще раз:)");
                    productName = in.nextLine(); //СЕТ НЕЙМ Х2
                }
                product.setName(productName);
            }

            System.out.println("Введите цену для товара:)");
            String productPrice = in.nextLine();
            Pattern pattern = Pattern.compile("^\\d+$");
            Matcher matcher = pattern.matcher(productPrice);
            if(matcher.matches() && Integer.parseInt(productPrice) > 0) {
                product.setPrice(Double.parseDouble(productPrice)); //СЕТ ПРАЙС
            } else {
                while(!matcher.matches() || Integer.parseInt(productPrice) < 1) {
                    System.out.println("Обнаружены символы помимо цифр либо число меньше нуля");
                    productPrice = in.nextLine(); //СЕТ ПРАЙС Х2
                }
                product.setPrice(Double.parseDouble(productPrice));
            }

            System.out.println("Введите характеристики для товара");
            List<Option> listOption = product.getCategory().getOption();
            for(Option s : listOption) {
                Value value = new Value();
                value.setProduct(product);
                value.setOption(s);
                System.out.println(s.getName() + ":");
                String valueString = in.nextLine();
                while(valueString.isEmpty()) {
                    System.out.println("Нельзя ввести пустую строку");
                    valueString = in.nextLine();
                }
                value.setValue(valueString);
                try {
                    manager.getTransaction().begin();
                    manager.persist(product);
                    manager.persist(value);
                    manager.getTransaction().commit();
                } catch (Exception e) {
                    manager.getTransaction().rollback();
                }

            }

            System.out.println("Товар был успешно добавлен:)");
    }
}
