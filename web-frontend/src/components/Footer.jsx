import { HStack } from '@chakra-ui/react'
import Logo from './Logo'
import instagram from 'super-tiny-icons/images/svg/instagram.svg'
import github from 'super-tiny-icons/images/svg/github.svg'
import strava from 'super-tiny-icons/images/svg/strava.svg'
import youtube from 'super-tiny-icons/images/svg/youtube.svg'
import twitter from 'super-tiny-icons/images/svg/twitter.svg'
import email from 'super-tiny-icons/images/svg/email.svg'
import facebook from 'super-tiny-icons/images/svg/facebook.svg'


const Social = () => {
    return <div className="col-md-4 md-margin-bottom-40">
        <div className="headline"><h2 className="heading-sm">Contact Us</h2></div>
        <HStack spacing='10px'>
            <Logo image={instagram} name='Instagram' link='https://www.instagram.com/peregin'/>
            <Logo image={github} name='GitHub' link='https://github.com/peregin/velocorner.com'/>
            <Logo image={strava} name='Strava' link='https://www.strava.com/clubs/velocorner'/>
            <Logo image={youtube} name='YouTube' link='https://www.youtube.com/channel/UCekBQAfLHviXTvB1kVjzBZQ'/>
            <Logo image={twitter} name='Twitter' link='https://twitter.com/velocorner_com'/>
            <Logo image={email} name='Email' link='mailto:velocorner.com@@gmail.com'/>
            <Logo image={facebook} name='Facebook' link='https://www.facebook.com/101313662146829'/>
        </HStack>
    </div>
}

const Copyright = () => {
    return <div className="copyright">
        <div className="container">
            <div className="col-md-4" />
            <div className="col-md-4">
                <p className="text-center">2015 - {(new Date().getFullYear())} &copy; All Rights Reserved <a style={{ color: '#3a5424', fontWeight: 'bold' }} target="_blank" rel="noopener noreferrer" href="https://velocorner.com">Velocorner</a></p>
            </div>
            <div className="col-md-4" style={{ textAlign: 'right' }}>
                <statuspage-widget src="https://velocorner.statuspage.io/"></statuspage-widget>
            </div>
        </div>
    </div>
}

const Footer = () => {
    return (
        <footer role="banner">

            <section>
                <div id="footer-v2" className="footer-v2">
                    <div className="footer">
                        <div className="container">
                            <div className="row">
                                <Instagram />
                                <Social />
                            </div>
                        </div>
                    </div>

                    <Copyright />

                </div>
            </section>

        </footer>
    )
}

export default Footer;