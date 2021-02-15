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
        if (doing.isEmpty()) {
            System.out.println("Всего доброго!");
            return;
        }
        while (Integer.parseInt(doing) != 1 && Integer.parseInt(doing) != 2) {
            System.out.println("Введите 1 для добавления товара" + "\nВведите 2 для изменения товара");
            doing = in.nextLine();
        }

        if (Integer.parseInt(doing) == 1) {
            Product product = new Product();
            System.out.println("Введите ID категории");
            String categoryNumber = in.nextLine();

            while (manager.find(Category.class, Long.parseLong(categoryNumber)) == null) {
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
            if (matcher.matches() && Integer.parseInt(productPrice) > 0) {
                product.setPrice(Double.parseDouble(productPrice)); //СЕТ ПРАЙС
            } else {
                while (!matcher.matches() || Integer.parseInt(productPrice) < 1) {
                    System.out.println("Обнаружены символы помимо цифр либо число меньше нуля");
                    productPrice = in.nextLine(); //СЕТ ПРАЙС Х2
                }
                product.setPrice(Double.parseDouble(productPrice));
            }

            System.out.println("Введите характеристики для товара");
            List<Option> listOption = product.getCategory().getOption();
            for (Option s : listOption) {
                Value value = new Value();
                value.setProduct(product);
                value.setOption(s);
                System.out.println(s.getName() + ":");
                String valueString = in.nextLine();
                while (valueString.isEmpty()) {
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
        } else if (Integer.parseInt(doing) == 2) { //ВАРИАНТ С ИЗМЕНЕНИЕМ ТОВАРА
            System.out.println("Введите номер товара, который вы хотите изменить");
            String numberOfProduct = in.nextLine();
            if (numberOfProduct.isEmpty()) {
                System.out.println("Всего доброго!");
                return;
            }
            while ((manager.find(Product.class, Long.parseLong(numberOfProduct)) == null)) {
                System.out.println("Товара с указанным номером не найден");
                numberOfProduct = in.nextLine();
            }
            Product product = manager.find(Product.class, Long.parseLong(numberOfProduct));
            System.out.println("Нажав Enter вы оставляете поле без изменений!");

            System.out.println("Выберите ID категории [" + product.getCategory().getId() + "]");
            String categoryNumber = in.nextLine();
            if (!categoryNumber.isEmpty()) {
                while (manager.find(Category.class, Long.parseLong(categoryNumber)) == null) {
                    System.out.println("Категории под данным числом не существует");
                    categoryNumber = in.nextLine(); //ДОБАВИТЬ В ТРАЙ В КОНЦЕ
                }
            } else {
                categoryNumber = String.valueOf(product.getCategory().getId());
            }


            System.out.println("Введите наименование товара [" + product.getName() + "]");
            String productName = in.nextLine();
            if (productName.isEmpty()) {
                productName = product.getName();
            }


            System.out.println("Введите цену для товара [" + product.getPrice() + "]");
            String productPrice = in.nextLine();
            if (!productPrice.isEmpty()) {
                Pattern pattern = Pattern.compile("^\\d+$");
                Matcher matcher = pattern.matcher(productPrice);
                if (matcher.matches() && Integer.parseInt(productPrice) > 0) {
                    product.setPrice(Double.parseDouble(productPrice)); //СЕТ ПРАЙС
                } else {
                    while (!matcher.matches() && Integer.parseInt(productPrice) < 0) {
                        System.out.println("Обнаружены символы помимо цифр либо число меньше нуля");
                        productPrice = in.nextLine(); //СЕТ ПРАЙС Х2 //ДОБАВЛЕНИЕ В КОНЦЕ
                    }
                }
            } else {
                productPrice = String.valueOf(product.getPrice());
            }

            try { //ДОБАВЛЕНИЕ КАТЕГОРИИ, ИМЕНИ, ЦЕНЫ
                manager.getTransaction().begin();
                product.setCategory(manager.find(Category.class, Long.parseLong(categoryNumber)));
                product.setName(productName);
                product.setPrice(Double.parseDouble(productPrice));
                manager.getTransaction().commit();
            } catch (Exception e) {
                manager.getTransaction().rollback();
            }

            System.out.println("Введите характеристики для этого товара");
            List<Option> optionList = product.getCategory().getOption();
            for (Option s : optionList) {
                List<Value> listOfNumberOfValuesId = manager.createQuery("select v from Value v where v.product = :pr and v.option = :pr2", Value.class)
                        .setParameter("pr", product)
                        .setParameter("pr2", s)
                        .getResultList();
                /////////////////////////////////////////////////
                if (listOfNumberOfValuesId.size() == 0) {
                    System.out.println("Была обнаружена незаполненная характеристика данной категории товара. Заполните ее.");
                    Value value = new Value();
                    value.setProduct(product);
                    value.setOption(s);
                    System.out.println(s.getName() + ":");
                    String valueString = in.nextLine();
                    while (valueString.isEmpty()) {
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

                    listOfNumberOfValuesId = manager.createQuery("select v from Value v where v.product = :pr and v.option = :pr2", Value.class)
                            .setParameter("pr", product)
                            .setParameter("pr2", s)
                            .getResultList();
                }
                //////////////////////////////////////////////////////

                Value value = listOfNumberOfValuesId.get(0);

                System.out.println(s.getName() + "[" + value.getValue() + "]");
                value.setProduct(product);
                value.setOption(s);
                String valueString = in.nextLine();
                if (valueString.isEmpty()) {
                    valueString = value.getValue();
                }
                value.setValue(valueString);
                try {
                    manager.getTransaction().begin();
                    value.setProduct(product);
                    value.setOption(s);
                    manager.getTransaction().commit();
                } catch (Exception e) {
                    manager.getTransaction().rollback();
                }


            }
            System.out.println("Данные были успешно изменены! (но это не точно)");

        }
    }
}
