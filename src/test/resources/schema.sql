CREATE TABLE subscription (
  num_sub BIGINT AUTO_INCREMENT PRIMARY KEY,
  start_date DATE,
  end_date DATE,
  price REAL,
  type_sub VARCHAR(255)
);

CREATE TABLE skier (
  num_skier BIGINT AUTO_INCREMENT PRIMARY KEY,
  first_name VARCHAR(255),
  last_name VARCHAR(255),
  date_of_birth DATE,
  city VARCHAR(255),
  subscription_num_sub BIGINT,
  CONSTRAINT fk_skier_subscription FOREIGN KEY (subscription_num_sub) REFERENCES subscription(num_sub)
);
