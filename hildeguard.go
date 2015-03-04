package main

import (
	"bufio"
	"fmt"
	"github.com/andyHa/go-otp"
	"log"
	"net"
	"os"
	"os/exec"
	"os/user"
	"strings"
	"time"
)

type account struct {
	name        string
	token       string
	acceptedIPs string
}

type AccountList []*account

func findTokensFile() string {
	usr, err := user.Current()
	if err != nil {
		log.Fatal(err)
	}
	return usr.HomeDir + "/.ssh/authorized_tokens"
}

func loadTokensFile() AccountList {
	var accounts AccountList
	filename := findTokensFile()

	file, err := os.Open(filename)
	if err != nil {
		fmt.Printf("%s cannot be read! \n", filename)
		return accounts
	}
	defer file.Close()

	scanner := bufio.NewScanner(file)
	line := 1
	for scanner.Scan() {
		textLine := strings.TrimSpace(scanner.Text())
		if !strings.HasPrefix(textLine, "#") {
			tuple := strings.Split(scanner.Text(), " ")
			if len(tuple) < 2 {
				fmt.Printf("Error in '%s' line %d: Expected a tuple like: <name> <token> [<ip>]!\n", filename, line)
			} else {
				account := &account{
					name:        tuple[0],
					token:       tuple[1],
					acceptedIPs: "",
				}
				if len(tuple) > 2 {
					account.acceptedIPs = tuple[2]
				}

				accounts = append(accounts, account)
			}
			line++
		}
	}

	if line == 1 {
		fmt.Printf("%s is empty...\n", filename)
	}

	return accounts
}

func verifyToken(accounts AccountList, token string) bool {
	filename := findTokensFile()

	file, err := os.Open(filename)
	if err != nil {
		fmt.Printf("%s does not exist! \n", filename)
		return false
	}
	defer file.Close()

	for _, a := range accounts {
		totp := &otp.TOTP{
			Secret: a.token,
		}

		if totp.Verify(token) {
			fmt.Printf("Verified token for '%s'\n", a.name)
			return true
		}
	}

	return false
}

func verifyIPforAccount(sshClientIp net.IP, account *account) bool {
	for _, acceptedIp := range strings.Split(account.acceptedIPs, ",") {
		if len(acceptedIp) > 0 {
			_, netMask, err := net.ParseCIDR(acceptedIp)
			if err == nil {
				// We can parse the given string as CIDR -> check if it contains the given IP
				if netMask.Contains(sshClientIp) {
					return true
				}
			} else {
				acceptedIp := net.ParseIP(acceptedIp)
				if acceptedIp == nil {
					fmt.Printf("Invalid IP/CIDR '%s' in account '%s'\n", acceptedIp, account.name)
				} else {
					if acceptedIp.Equal(sshClientIp) {
						return true
					}
				}
			}
		}
	}
	return false
}

func verifyIp(accounts AccountList) bool {
	sshClient := os.Getenv("SSH_CLIENT")
	if len(sshClient) == 0 {
		return false
	}
	sshClientData := strings.Split(sshClient, " ")
	sshClientIp := net.ParseIP(sshClientData[0])
	if sshClientIp == nil {
		fmt.Printf("Cannot parse SSH_CLIENT ip '%s'\n", sshClientData[0])
		return false
	}
	for _, a := range accounts {
		if verifyIPforAccount(sshClientIp, a) {
			fmt.Printf("Verified IP for '%s'\n", a.name)
			return true
		}
	}
	return false
}

func runShell() {
	subProcess := exec.Cmd{
		Path: "/bin/bash",
	}
	subProcess.Stdin = os.Stdin
	subProcess.Stdout = os.Stdout
	subProcess.Stderr = os.Stderr

	if err := subProcess.Run(); err != nil {
		log.Fatal(err)
	}
}

func main() {
	fmt.Printf("\nHildeGUARD is watching you! (%s)\nhttps://github.com/scireum/hildeguard\n\n", time.Now().Format(time.ANSIC))

	accounts := loadTokensFile()

	if len(accounts) == 0 {
		fmt.Println("~/.ssh/authorized_tokens does not contain any accounts!")
		fmt.Println("")
		fmt.Println("Please enumerate accepted tokens like this:")
		fmt.Println("<name> <token> [<ip>]")
		fmt.Println("If you provide an ip address (can also be a subnet in CIDR notation),")
		fmt.Println("this account can logon from this ip without providing a token.")
		fmt.Println("Separate multiple ips with a comma.")
		fmt.Println("")
		fmt.Println("Granting access due to invalid configuration!")
		runShell()
	} else {
		if verifyIp(accounts) {
			runShell()
		} else {
			fmt.Print("Please enter your security token: ")
			reader := bufio.NewReader(os.Stdin)
			token, _ := reader.ReadString('\n')

			if verifyToken(accounts, strings.TrimSpace(token)) {
				runShell()
			} else {
				fmt.Println("Cannot authenticate you - sorry!")
			}
		}
	}
}
