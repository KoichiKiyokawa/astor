# ASTOR: A Program Repair Library for Java

This project is an extension of [Astor](https://hal.archives-ouvertes.fr/hal-01321615/document)

## How to run
1. You have to install [Docker](https://docs.docker.com).

2. You have to pull image of Astor.
```
docker pull tdurieux/astor
```

3. Then, you have to clone this repository and make jar file.
```
git clone https://github.com/KoichiKiyokawa/astor.git
mvn package -DskipTests=true
```

4. You can run LevenBased algorithm by following command if you fix bug of math_1(cf: [Defects4j]()) in local scope.
```
docker run --rm -v <path to clone of this repository>:/astor tdurieux/astor -i math_1 --parameters mode:leven > math_1.out &
```
(math_1.out show the result of fix.)

5. You can run PurposeBased algorithm by following command if you fix bug of math_1(cf: [Defects4j]()) in package scope.
```
docker run --rm -v <path to clone of this repository>:/astor tdurieux/astor -i math_1 --scope package --parameters mode:purpose > math_1.out &
```